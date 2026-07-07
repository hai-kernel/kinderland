package kinderland.order.service;

import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.order.client.ProductClient;
import kinderland.order.client.PaymentClient;
import kinderland.order.client.dto.ProductInternalResponse;
import kinderland.order.client.dto.InitiatePaymentRequest;
import kinderland.order.client.dto.PaymentInitResponse;
import kinderland.order.event.OrderCreatedEvent;
import kinderland.order.event.OrderCancelledEvent;
import kinderland.order.event.OrderEventPublisher;
import kinderland.order.mapper.OrderMapper;
import kinderland.order.model.dto.request.CreateOrderRequest;
import kinderland.order.model.dto.request.OrderLineRequest;
import kinderland.order.model.dto.response.CheckoutResponse;
import kinderland.order.model.dto.response.OrderResponse;
import kinderland.order.model.entity.Order;
import kinderland.order.model.entity.OrderItem;
import kinderland.order.model.entity.OrderStatus;
import kinderland.order.model.entity.PaymentMethod;
import kinderland.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderService {

    OrderRepository orderRepository;
    ProductClient productClient;
    PaymentClient paymentClient;
    OrderEventPublisher orderEventPublisher;
    OrderMapper orderMapper;

    @Transactional
    public OrderResponse createOrder(String accountEmail, CreateOrderRequest request) {
        Order order = Order.builder()
                .accountEmail(accountEmail)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        // 1) Với mỗi dòng: gọi Product Service (Feign) lấy GIÁ & TỒN KHO chuẩn.
        for (OrderLineRequest line : request.getItems()) {
            ProductInternalResponse product = fetchProduct(line.getProductId());

            if (!product.isActive()) {
                throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
            }
            if (product.getStockQuantity() < line.getQuantity()) {
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(line.getQuantity()));
            total = total.add(lineTotal);

            items.add(OrderItem.builder()
                    .order(order)
                    .productId(product.getId())
                    .productName(product.getName())      // denormalize: chốt tên & giá tại thời điểm đặt
                    .unitPrice(product.getPrice())
                    .quantity(line.getQuantity())
                    .lineTotal(lineTotal)
                    .build());
        }

        order.setItems(items);
        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);

        // 2) Bắn event để Product Service TRỪ KHO (Saga bất đồng bộ).
        //    Tách khỏi luồng đồng bộ: đơn vẫn tạo thành công kể cả khi trừ kho xử lý sau.
        publishOrderCreated(saved);

        return orderMapper.toResponse(saved);
    }

    private void publishOrderCreated(Order order) {
        List<OrderCreatedEvent.Item> eventItems = order.getItems().stream()
                .map(i -> OrderCreatedEvent.Item.builder()
                        .productId(i.getProductId())
                        .quantity(i.getQuantity())
                        .build())
                .toList();

        orderEventPublisher.publishOrderCreated(OrderCreatedEvent.builder()
                .orderId(order.getId())
                .accountEmail(order.getAccountEmail())
                .items(eventItems)
                .build());
    }

    public List<OrderResponse> getMyOrders(String accountEmail) {
        return orderRepository.findByAccountEmailOrderByCreatedAtDesc(accountEmail).stream()
                .map(orderMapper::toResponse).toList();
    }

    public OrderResponse getById(Long id, String accountEmail) {
        return orderMapper.toResponse(loadOwnedOrder(id, accountEmail));
    }

    /**
     * Chủ đơn checkout: gọi payment-service (Feign) khởi tạo thanh toán.
     * Đơn GIỮ PENDING; chỉ chuyển PAID khi nhận PaymentCompletedEvent (Kafka) từ payment-service.
     */
    @Transactional
    public CheckoutResponse checkout(Long id, String accountEmail, PaymentMethod method, String ipAddress) {
        Order order = loadOwnedOrder(id, accountEmail);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }

        PaymentInitResponse pay = initiatePayment(order, method, ipAddress);
        String message = "VNPAY".equals(pay.getMethod())
                ? "Vui lòng mở paymentUrl để thanh toán qua VNPay"
                : "Đã ghi nhận thanh toán COD, đơn sẽ chuyển PAID";

        return CheckoutResponse.builder()
                .orderId(order.getId())
                .paymentMethod(pay.getMethod())
                .paymentStatus(pay.getStatus())
                .paymentUrl(pay.getPaymentUrl())
                .message(message)
                .build();
    }

    /** Set đơn PAID khi payment-service báo thanh toán thành công (gọi từ Kafka consumer). Idempotent. */
    @Transactional
    public void markPaid(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
        } else {
            log.info("Bỏ qua set PAID cho orderId={} vì trạng thái hiện tại là {}", orderId, order.getStatus());
        }
    }

    /** Chủ đơn huỷ đơn: chỉ khi đơn chưa giao/hoàn tất và chưa bị huỷ. Bắn event để product hoàn kho. */
    @Transactional
    public OrderResponse cancelOrder(Long id, String accountEmail) {
        Order order = loadOwnedOrder(id, accountEmail);
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PAID) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }
        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        publishOrderCancelled(saved);   // product-service cộng kho lại
        return orderMapper.toResponse(saved);
    }

    private PaymentInitResponse initiatePayment(Order order, PaymentMethod method, String ipAddress) {
        try {
            return paymentClient.initiate(InitiatePaymentRequest.builder()
                    .orderId(order.getId())
                    .accountEmail(order.getAccountEmail())
                    .amount(order.getTotalAmount())
                    .method(method)
                    .ipAddress(ipAddress)
                    .build());
        } catch (FeignException e) {
            log.error("Gọi Payment Service lỗi (orderId={}): {}", order.getId(), e.getMessage());
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
        }
    }

    private void publishOrderCancelled(Order order) {
        List<OrderCancelledEvent.Item> items = order.getItems().stream()
                .map(i -> OrderCancelledEvent.Item.builder()
                        .productId(i.getProductId())
                        .quantity(i.getQuantity())
                        .build())
                .toList();

        orderEventPublisher.publishOrderCancelled(OrderCancelledEvent.builder()
                .orderId(order.getId())
                .accountEmail(order.getAccountEmail())
                .items(items)
                .build());
    }

    /** ADMIN chuyển trạng thái đơn trong quá trình xử lý (PAID -> SHIPPING -> COMPLETED...). */
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus, String role) {
        if (!"ROLE_ADMIN".equals(role)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        if (order.getStatus() == OrderStatus.CANCELLED) {
            // Đơn đã huỷ là trạng thái kết thúc, không chuyển tiếp được.
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }
        order.setStatus(newStatus);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    /** Lấy đơn theo id và đảm bảo nó thuộc về chính user đang gọi (chống xem/sửa đơn người khác). */
    private Order loadOwnedOrder(Long id, String accountEmail) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        if (!order.getAccountEmail().equals(accountEmail)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        return order;
    }

    private ProductInternalResponse fetchProduct(Long productId) {
        try {
            return productClient.getProduct(productId);
        } catch (FeignException.NotFound e) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        } catch (FeignException e) {
            log.error("Gọi Product Service lỗi (productId={}): {}", productId, e.getMessage());
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
        }
    }

}

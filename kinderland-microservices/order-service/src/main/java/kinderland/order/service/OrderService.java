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
import kinderland.order.client.dto.SkuInternalResponse;
import kinderland.order.client.dto.InitiatePaymentRequest;
import kinderland.order.client.dto.PaymentInitResponse;
import kinderland.order.event.OrderCreatedEvent;
import kinderland.order.event.OrderCancelledEvent;
import kinderland.order.event.OrderEventPublisher;
import kinderland.order.mapper.OrderMapper;
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
    LoyaltyService loyaltyService;

    /** Đơn PENDING quá số phút này sẽ bị tự huỷ + hoàn kho. */
    @org.springframework.beans.factory.annotation.Value("${order.pending-expiry-minutes:15}")
    @lombok.experimental.NonFinal
    int pendingExpiryMinutes;

    /**
     * Tạo đơn theo mô hình SKU + Store (khớp FE): mỗi dòng = {skuId, quantity}, cả đơn giao tại 1 store
     * tới 1 địa chỉ. Với mỗi dòng: Feign lấy giá SKU + kiểm tồn TẠI store, chốt snapshot.
     */
    @Transactional
    public OrderResponse createOrder(String accountEmail, Long addressId, Long storeId, List<OrderLineRequest> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new AppException(ErrorCode.CART_EMPTY);
        }
        Order order = Order.builder()
                .accountEmail(accountEmail)
                .storeId(storeId)
                .addressId(addressId)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (OrderLineRequest line : lines) {
            SkuInternalResponse sku = fetchSku(line.getSkuId(), storeId);
            if (sku.getAvailableQuantity() == null || sku.getAvailableQuantity() < line.getQuantity()) {
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }
            BigDecimal unitPrice = sku.getPrice() == null ? BigDecimal.ZERO : sku.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(line.getQuantity()));
            total = total.add(lineTotal);

            items.add(OrderItem.builder()
                    .order(order)
                    .skuId(sku.getSkuId())
                    .skuCode(sku.getSkuCode())
                    .productName(sku.getProductName())     // denormalize snapshot
                    .imageUrl(sku.getImageUrl())
                    .unitPrice(unitPrice)
                    .quantity(line.getQuantity())
                    .lineTotal(lineTotal)
                    .build());
        }

        order.setItems(items);
        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);

        publishOrderCreated(saved);   // product trừ kho theo (sku, store) qua Kafka
        return orderMapper.toResponse(saved);
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
    public CheckoutResponse checkout(Long id, String accountEmail, PaymentMethod method,
                                     Integer pointsToUse, String ipAddress) {
        Order order = loadOwnedOrder(id, accountEmail);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }

        applyLoyaltyDiscount(order, accountEmail, pointsToUse);

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

    /** Set đơn PAID khi payment-service báo thanh toán thành công (Kafka consumer). Idempotent. */
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

    /**
     * Chủ đơn huỷ đơn: CHỈ khi đơn còn PENDING (chưa thanh toán). Bắn event để product hoàn kho.
     * Đơn đã PAID phải đi qua luồng trả hàng/hoàn tiền (ReturnRequest), không huỷ trực tiếp.
     */
    @Transactional
    public OrderResponse cancelOrder(Long id, String accountEmail) {
        Order order = loadOwnedOrder(id, accountEmail);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }
        return orderMapper.toResponse(cancelAndRefund(order));
    }

    /** Tự huỷ đơn PENDING quá hạn + hoàn kho (gọi định kỳ bởi OrderExpiryScheduler). */
    @Transactional
    public int cancelExpiredPendingOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(pendingExpiryMinutes);
        List<Order> expired = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING, cutoff);
        expired.forEach(this::cancelAndRefund);
        if (!expired.isEmpty()) {
            log.info("Đã tự huỷ {} đơn PENDING quá hạn (>{} phút) + hoàn kho", expired.size(), pendingExpiryMinutes);
        }
        return expired.size();
    }

    private Order cancelAndRefund(Order order) {
        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        publishOrderCancelled(saved);   // product hoàn kho theo (sku, store)
        return saved;
    }

    /** ADMIN chuyển trạng thái đơn (PAID -> SHIPPING -> COMPLETED...). */
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus, String role) {
        if (!"ROLE_ADMIN".equals(role)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }
        // Tích điểm khi đơn chuyển COMPLETED (1 lần / đơn) — tính trên số tiền thực trả (đã trừ giảm điểm).
        if (newStatus == OrderStatus.COMPLETED && !order.isPointsAwarded()) {
            loyaltyService.awardPoints(order.getAccountEmail(), order.getTotalAmount());
            order.setPointsAwarded(true);
        }

        order.setStatus(newStatus);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    // ---------- helpers ----------

    /**
     * Áp điểm loyalty lúc checkout: trừ điểm, giảm trực tiếp totalAmount (số tiền cần thanh toán).
     * Chỉ áp 1 lần / đơn (guard theo pointsUsed) để checkout lặp không trừ điểm nhiều lần.
     */
    private void applyLoyaltyDiscount(Order order, String accountEmail, Integer pointsToUse) {
        if (pointsToUse == null || pointsToUse <= 0 || order.getPointsUsed() != null && order.getPointsUsed() > 0) {
            return;
        }
        BigDecimal discount = loyaltyService.usePoints(accountEmail, pointsToUse);
        if (discount.compareTo(order.getTotalAmount()) > 0) {
            discount = order.getTotalAmount();
        }
        order.setPointsUsed(pointsToUse);
        order.setPointsDiscount(discount);
        order.setTotalAmount(order.getTotalAmount().subtract(discount));
        orderRepository.save(order);
    }

    private void publishOrderCreated(Order order) {
        orderEventPublisher.publishOrderCreated(OrderCreatedEvent.builder()
                .orderId(order.getId())
                .accountEmail(order.getAccountEmail())
                .items(order.getItems().stream()
                        .map(i -> OrderCreatedEvent.Item.builder()
                                .skuId(i.getSkuId()).storeId(order.getStoreId()).quantity(i.getQuantity())
                                .build())
                        .toList())
                .build());
    }

    private void publishOrderCancelled(Order order) {
        orderEventPublisher.publishOrderCancelled(OrderCancelledEvent.builder()
                .orderId(order.getId())
                .accountEmail(order.getAccountEmail())
                .items(order.getItems().stream()
                        .map(i -> OrderCancelledEvent.Item.builder()
                                .skuId(i.getSkuId()).storeId(order.getStoreId()).quantity(i.getQuantity())
                                .build())
                        .toList())
                .build());
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

    private Order loadOwnedOrder(Long id, String accountEmail) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        if (!order.getAccountEmail().equals(accountEmail)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        return order;
    }

    private SkuInternalResponse fetchSku(Long skuId, Long storeId) {
        try {
            return productClient.getSku(skuId, storeId);
        } catch (FeignException.NotFound e) {
            throw new AppException(ErrorCode.SKU_NOT_FOUND);
        } catch (FeignException e) {
            log.error("Gọi Product Service lỗi (skuId={}): {}", skuId, e.getMessage());
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
        }
    }
}

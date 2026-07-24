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
import kinderland.order.client.PromotionClient;
import kinderland.order.client.dto.PaymentInitResponse;
import kinderland.order.client.dto.PromotionValidationResponse;
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
    PromotionClient promotionClient;
    PaymentClient paymentClient;
    OrderEventPublisher orderEventPublisher;
    OrderMapper orderMapper;
    LoyaltyService loyaltyService;

    /** Đơn PENDING quá số phút này sẽ bị tự huỷ + hoàn kho. */
    @org.springframework.beans.factory.annotation.Value("${order.pending-expiry-minutes:15}")
    @lombok.experimental.NonFinal
    int pendingExpiryMinutes;

    /** Ngưỡng miễn phí ship + phí ship mặc định (VND). Khớp quy tắc FE đang hiển thị. */
    @org.springframework.beans.factory.annotation.Value("${order.free-shipping-threshold:500000}")
    @lombok.experimental.NonFinal
    BigDecimal freeShippingThreshold;

    @org.springframework.beans.factory.annotation.Value("${order.shipping-fee:30000}")
    @lombok.experimental.NonFinal
    BigDecimal shippingFee;

    /**
     * Tạo đơn theo mô hình SKU + Store (khớp FE): mỗi dòng = {skuId, quantity}, cả đơn giao tại 1 store
     * tới 1 địa chỉ. Với mỗi dòng: Feign lấy giá SKU + kiểm tồn TẠI store, chốt snapshot.
     */
    @Transactional
    public OrderResponse createOrder(String accountEmail, Long addressId, Long storeId,
                                     List<OrderLineRequest> lines, String promotionCode) {
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

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (OrderLineRequest line : lines) {
            SkuInternalResponse sku = fetchSku(line.getSkuId(), storeId);
            if (sku.getAvailableQuantity() == null || sku.getAvailableQuantity() < line.getQuantity()) {
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }
            // sku.getPrice() = giá SAU khuyến mãi do product-service chốt (KHÔNG nhận giá từ FE).
            BigDecimal unitPrice = sku.getPrice() == null ? BigDecimal.ZERO : sku.getPrice();
            BigDecimal originalUnitPrice = sku.getOriginalPrice() == null ? unitPrice : sku.getOriginalPrice();
            BigDecimal qty = BigDecimal.valueOf(line.getQuantity());
            BigDecimal lineTotal = unitPrice.multiply(qty);
            BigDecimal lineDiscount = originalUnitPrice.subtract(unitPrice).max(BigDecimal.ZERO).multiply(qty);
            subtotal = subtotal.add(lineTotal);

            items.add(OrderItem.builder()
                    .order(order)
                    .skuId(sku.getSkuId())
                    .productId(sku.getProductId())         // denormalize snapshot
                    .skuCode(sku.getSkuCode())
                    .size(sku.getSize())
                    .color(sku.getColor())
                    .productName(sku.getProductName())
                    .imageUrl(sku.getImageUrl())
                    .unitPrice(unitPrice)
                    .originalUnitPrice(originalUnitPrice)
                    .productDiscountAmount(lineDiscount)
                    .promotionId(sku.getPromotionId())
                    .quantity(line.getQuantity())
                    .lineTotal(lineTotal)
                    .build());

            // [PROMOTION DEBUG] Số THỰC TẾ được lưu vào order_items — đây là dòng cuối cùng
            // trong chuỗi. Nếu log này đúng (unitPrice < originalUnitPrice) mà Payment amount
            // vẫn sai, lỗi nằm ở initiatePayment/payment-service; nếu log này đã sai thì lỗi
            // nằm ở fetchSku()/Feign call phía trên (product-service trả sai, hoặc gọi nhầm
            // instance product-service chưa deploy code mới do load-balancing nhiều instance).
            log.info("[PROMOTION DEBUG] order-item skuId={} promotionId={} original={} " +
                            "discount(line)={} final(unit)={} lineTotal={}",
                    sku.getSkuId(), sku.getPromotionId(), originalUnitPrice, lineDiscount, unitPrice, lineTotal);
        }
        log.info("[PROMOTION DEBUG] order subtotal={}", subtotal);

        // Phí ship + giảm giá đều tính TẠI ĐÂY từ giá SKU do product-service trả về.
        // Client chỉ gửi {skuId, quantity, promotionCode} — mọi con số tiền đều do server chốt,
        // nên FE có sửa payload cũng không đổi được số tiền phải trả.
        BigDecimal shippingFee = calculateShippingFee(subtotal);
        BigDecimal discountAmount = resolvePromotion(order, promotionCode, subtotal);

        order.setItems(items);
        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(finalAmount(subtotal, shippingFee, discountAmount, BigDecimal.ZERO));
        Order saved = orderRepository.save(order);

        publishOrderCreated(saved);   // product trừ kho theo (sku, store) qua Kafka
        return orderMapper.toResponse(saved);
    }

    public List<OrderResponse> getMyOrders(String accountEmail) {
        return orderRepository.findByAccountEmailOrderByCreatedAtDesc(accountEmail).stream()
                .map(orderMapper::toResponse).toList();
    }

    /** ADMIN/MANAGER: tất cả đơn (mới nhất trước) cho trang quản trị đơn hàng của FE. */
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(orderMapper::toResponse).toList();
    }

    /** Đơn của 1 cửa hàng (manager xem đơn store mình) — FE gọi GET /orders/store/{storeId}. */
    public List<OrderResponse> getOrdersByStore(Long storeId) {
        return orderRepository.findByStoreIdOrderByCreatedAtDesc(storeId).stream()
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
            redeemPromotionIfNeeded(order);   // voucher chỉ "đã dùng" sau khi thanh toán thành công
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

    /**
     * ADMIN / MANAGER chuyển trạng thái đơn (PAID -> DELIVERED -> COMPLETED...).
     *
     * MANAGER được phép vì nghiệp vụ giao hàng nằm ở cửa hàng: màn hình
     * "Quản lý đơn hàng" của manager có nút "Giao hàng" gọi đúng endpoint này, nhưng
     * trước đây chỉ cho ROLE_ADMIN nên manager luôn nhận 403 — nút không bao giờ dùng
     * được.
     */
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus, String role) {
        if (!"ROLE_ADMIN".equals(role) && !"ROLE_MANAGER".equals(role)) {
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
        // Tính LẠI từ các thành phần gốc thay vì trừ dần vào totalAmount: trừ dần khiến số tiền
        // phụ thuộc vào thứ tự/số lần gọi, còn ở đây totalAmount luôn = công thức đầy đủ.
        order.setTotalAmount(finalAmount(order.getSubtotal(), order.getShippingFee(),
                order.getDiscountAmount(), discount));
        orderRepository.save(order);
    }

    /**
     * Áp mã khuyến mãi: hỏi product-service (nguồn sự thật) rồi ghi id + code vào đơn.
     * Mã không hợp lệ (hết hạn / chưa bắt đầu / bị khoá / hết lượt / chưa đủ điều kiện)
     * làm HỎNG luôn việc tạo đơn — thà báo lỗi rõ ràng còn hơn âm thầm tạo đơn giá đầy đủ
     * trong khi người dùng tin là đã được giảm.
     */
    private BigDecimal resolvePromotion(Order order, String promotionCode, BigDecimal subtotal) {
        if (promotionCode == null || promotionCode.isBlank()) {
            return BigDecimal.ZERO;
        }
        PromotionValidationResponse result;
        try {
            result = promotionClient.validate(promotionCode.trim().toUpperCase(), subtotal);
        } catch (FeignException e) {
            log.error("Gọi Product Service validate mã khuyến mãi lỗi (code={}): {}", promotionCode, e.getMessage());
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
        }
        if (result == null || !result.isValid()) {
            throw new AppException(ErrorCode.PROMOTION_INVALID);
        }

        order.setPromotionId(result.getPromotionId());
        order.setPromotionCode(result.getCode());
        BigDecimal discount = result.getDiscountAmount() == null ? BigDecimal.ZERO : result.getDiscountAmount();
        // Chặn phòng thủ: dù product-service có trả số lạ, giảm giá không bao giờ vượt tiền hàng.
        return discount.max(BigDecimal.ZERO).min(subtotal);
    }

    /** Miễn phí ship từ ngưỡng cấu hình; quy tắc nằm ở BE để FE và DB không thể lệch nhau. */
    private BigDecimal calculateShippingFee(BigDecimal subtotal) {
        return subtotal.compareTo(freeShippingThreshold) >= 0 ? BigDecimal.ZERO : shippingFee;
    }

    /** finalAmount = subtotal + ship - giảm giá mã - giảm giá điểm, không bao giờ âm. */
    private BigDecimal finalAmount(BigDecimal subtotal, BigDecimal shipping,
                                   BigDecimal discount, BigDecimal pointsDiscount) {
        return nz(subtotal).add(nz(shipping)).subtract(nz(discount)).subtract(nz(pointsDiscount))
                .max(BigDecimal.ZERO);
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * Ghi nhận lượt dùng mã — CHỈ khi đơn đã thanh toán thành công, và tối đa 1 lần/đơn.
     * Lỗi ở bước này KHÔNG được làm hỏng việc set PAID: tiền đã vào rồi, đơn phải PAID;
     * lượt voucher lệch là vấn đề nhỏ hơn nhiều so với đơn đã trả tiền mà kẹt PENDING.
     */
    private void redeemPromotionIfNeeded(Order order) {
        if (order.getPromotionId() == null || order.isPromotionRedeemed()) {
            return;
        }
        try {
            boolean redeemed = promotionClient.redeem(order.getPromotionId());
            if (!redeemed) {
                log.warn("Mã {} đã hết lượt khi ghi nhận cho orderId={} (đơn vẫn giữ mức giảm đã chốt)",
                        order.getPromotionCode(), order.getId());
            }
            order.setPromotionRedeemed(true);
        } catch (FeignException e) {
            log.error("Không ghi nhận được lượt dùng mã {} cho orderId={}: {}",
                    order.getPromotionCode(), order.getId(), e.getMessage());
        }
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

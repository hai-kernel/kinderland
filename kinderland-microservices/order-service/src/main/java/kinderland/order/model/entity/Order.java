package kinderland.order.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Định danh khách hàng = email (subject JWT, do Gateway truyền qua header). KHÔNG còn FK Account. */
    @Column(nullable = false)
    private String accountEmail;

    /** Cửa hàng nhận đơn + địa chỉ giao (tham chiếu lỏng; addressId thuộc auth-service). */
    private Long storeId;
    private Long addressId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    /** Tổng tiền hàng TRƯỚC mọi khoản giảm và phí ship (= tổng lineTotal). */
    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    /** Phí vận chuyển do BACKEND tính theo subtotal (không nhận từ client). */
    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO;

    /** Số tiền giảm từ mã khuyến mãi (KHÔNG bao gồm giảm bằng điểm — xem pointsDiscount). */
    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /** Mã khuyến mãi đã áp: giữ cả id (đối chiếu) lẫn code (snapshot, mã có thể bị đổi/xoá sau). */
    private Long promotionId;

    @Column(length = 50)
    private String promotionCode;

    /**
     * Đã ghi nhận lượt dùng mã cho đơn này chưa. Guard để PaymentCompletedEvent bị gửi lại
     * (Kafka at-least-once) không đốt thêm lượt của voucher.
     */
    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean promotionRedeemed = false;

    /**
     * SỐ TIỀN THỰC PHẢI TRẢ = subtotal + shippingFee - discountAmount - pointsDiscount.
     * Đây là con số gửi sang payment-service và dùng để tích điểm; giữ tên cũ totalAmount
     * để không phá payment/financial/loyalty đang đọc field này.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    /** Điểm loyalty đã dùng khi checkout + số VND được giảm tương ứng (0 nếu không dùng). */
    @Builder.Default
    private Integer pointsUsed = 0;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal pointsDiscount = BigDecimal.ZERO;

    /** Đánh dấu đã tích điểm cho đơn này (tránh tích 2 lần khi cập nhật trạng thái).
     *  columnDefinition có DEFAULT để ddl-auto=update thêm được cột NOT NULL vào bảng orders đã có dữ liệu. */
    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean pointsAwarded = false;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime createdAt;
}

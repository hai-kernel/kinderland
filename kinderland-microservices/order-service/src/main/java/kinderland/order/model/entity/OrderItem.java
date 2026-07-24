package kinderland.order.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Dòng đơn hàng theo SKU. skuId tham chiếu lỏng tới product-service (không FK).
 * skuCode/productName/unitPrice/imageUrl DENORMALIZE (chốt tại thời điểm đặt) để đơn bất biến.
 */
@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @Column(nullable = false)
    private Long skuId;

    private Long productId;
    private String skuCode;
    private String size;
    private String color;
    @Column(length = 500)
    private String productName;

    /**
     * TEXT chứ không phải varchar(255) mặc định.
     *
     * product-service trả về presigned S3 URL (S3Service.resolveImageUrl) — chuỗi
     * dài 700–900 ký tự vì mang theo X-Amz-Credential/X-Amz-Signature. Cột 255 ký tự
     * làm INSERT order_items nổ "value too long for type character varying(255)",
     * và vì insert nằm trong transaction tạo đơn nên toàn bộ POST /orders/create trả 500.
     */
    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    /** Đơn giá THỰC TRẢ (đã áp khuyến mãi cấp sản phẩm). lineTotal = unitPrice * quantity. */
    @Column(precision = 12, scale = 2)
    private BigDecimal unitPrice;

    /** Snapshot giá gốc trước khuyến mãi + tiền giảm CẢ DÒNG, để hiển thị & đối soát về sau. */
    @Column(precision = 12, scale = 2)
    private BigDecimal originalUnitPrice;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal productDiscountAmount = BigDecimal.ZERO;

    /** Snapshot promotion đã áp cho dòng này (null nếu không có). */
    private Long promotionId;

    private Integer quantity;

    @Column(precision = 12, scale = 2)
    private BigDecimal lineTotal;
}

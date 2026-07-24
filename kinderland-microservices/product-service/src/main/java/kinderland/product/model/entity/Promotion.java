package kinderland.product.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_id")
    private Long promotionId;

    @Column(name = "code", unique = true, length = 50)
    private String code;

    @Column(name = "promotion_title")
    private String title;

    @Column(name = "promotion_desc", columnDefinition = "TEXT")
    private String description;

    @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    /**
     * Cho phép khoá mã thủ công mà không cần xoá (giữ lịch sử đơn đã dùng).
     * columnDefinition có DEFAULT để ddl-auto=update thêm được cột vào bảng đã có dữ liệu.
     */
    @Builder.Default
    @Column(name = "active", nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;

    /** Tổng số lượt được dùng; null = không giới hạn. */
    @Column(name = "usage_limit")
    private Integer usageLimit;

    /** Số lượt đã dùng — CHỈ tăng khi đơn thanh toán thành công. */
    @Builder.Default
    @Column(name = "used_count", nullable = false, columnDefinition = "integer default 0")
    private Integer usedCount = 0;

    /** Giá trị đơn tối thiểu (tính trên subtotal) để được áp mã; null = không yêu cầu. */
    @Column(name = "min_order_amount", precision = 12, scale = 2)
    private BigDecimal minOrderAmount;

    /** Trần số tiền được giảm; null = không giới hạn. */
    @Column(name = "max_discount_amount", precision = 12, scale = 2)
    private BigDecimal maxDiscountAmount;
}

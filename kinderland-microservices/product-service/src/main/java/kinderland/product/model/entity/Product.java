package kinderland.product.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Đơn giản hoá so với monolith: gộp giá & tồn kho trực tiếp lên Product
 * (monolith tách minPrice→Sku, tồn kho→Inventory). Đủ để Internal API trả giá/tồn kho.
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    private String ageRange;
    private String gender;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;

    /**
     * Trạng thái KINH DOANH: còn bán hay tạm ngừng bán. Do admin bật/tắt chủ động.
     * KHÁC hoàn toàn {@link #deleted} — một sản phẩm có thể ngừng bán mà chưa bị xoá.
     */
    @Builder.Default
    private boolean active = true;

    /**
     * XOÁ MỀM. Sản phẩm bị sku -> inventory/reviews/transfer_orders tham chiếu, và bởi
     * order_items ở order-service (database KHÁC, không có FK để chặn). Xoá cứng sẽ ném
     * FK violation, hoặc tệ hơn là làm đơn hàng cũ mất thông tin sản phẩm đã bán.
     *
     * Tách riêng khỏi 'active' CÓ CHỦ Ý: nếu dùng chung một cờ thì khôi phục một sản
     * phẩm vốn đang ngừng bán sẽ vô tình bật nó bán lại.
     *
     * columnDefinition có DEFAULT false: ddl-auto=update thêm cột NOT NULL vào bảng
     * đã có dữ liệu thì PostgreSQL bắt buộc phải có DEFAULT, thiếu là ALTER TABLE fail.
     */
    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean not null default false")
    private boolean deleted = false;

    /** Thời điểm xoá mềm — phục vụ truy vết. null = chưa từng bị xoá. */
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;
}

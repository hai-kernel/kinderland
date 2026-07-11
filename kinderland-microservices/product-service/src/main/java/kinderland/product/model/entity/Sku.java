package kinderland.product.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Biến thể sản phẩm (size/màu/loại), có GIÁ riêng. Ảnh SKU lưu ở bảng images (entityType=SKU). */
@Entity
@Table(name = "sku")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sku {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private String skuCode;
    private String size;
    private String color;
    private String type;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;
}

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
    private String productName;
    private String imageUrl;

    @Column(precision = 12, scale = 2)
    private BigDecimal unitPrice;

    private Integer quantity;

    @Column(precision = 12, scale = 2)
    private BigDecimal lineTotal;
}

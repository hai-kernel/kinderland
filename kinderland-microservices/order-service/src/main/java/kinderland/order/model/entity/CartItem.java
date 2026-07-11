package kinderland.order.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonIgnore
    private Cart cart;

    /** Biến thể sản phẩm (tham chiếu lỏng tới product-service). */
    @Column(nullable = false)
    private Long skuId;

    /** Cửa hàng mua tại (tồn kho theo store). */
    @Column(nullable = false)
    private Long storeId;

    private Integer quantity;
}

package kinderland.product.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Phiếu chuyển kho giữa 2 cửa hàng (state machine). createdBy = email (thay FK Account của monolith).
 * Store & Sku cùng service nên giữ @ManyToOne.
 */
@Entity
@Table(name = "transfer_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "from_store_id")
    private Store fromStore;

    @ManyToOne(optional = false)
    @JoinColumn(name = "to_store_id")
    private Store toStore;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sku_id")
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    private Sku sku;

    private Integer quantity;

    @Enumerated(EnumType.STRING)
    private TransferStatus status;

    @Column(name = "created_by")
    private String createdByEmail;

    private LocalDateTime createdAt;
}

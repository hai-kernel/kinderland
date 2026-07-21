package kinderland.product.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Đánh giá theo SKU. Danh tính người đánh giá = email (thay FK Account của monolith).
 * productId denormalize từ Sku.product để truy vấn review theo product nhanh.
 */
@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_email", nullable = false)
    private String accountEmail;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sku_id", nullable = false)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    private Sku sku;

    @Column(nullable = false)
    private int rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(columnDefinition = "TEXT")
    private String managerReply;

    @Column(name = "manager_reply_at")
    private LocalDateTime managerReplyAt;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (sku != null && sku.getProduct() != null) {
            productId = sku.getProduct().getId();
        }
    }
}

package kinderland.order.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Điểm tích luỹ của khách hàng. Keyed by accountEmail (subject JWT) — KHÔNG còn FK Account
 * như monolith (order-service không giữ bảng Account).
 */
@Entity
@Table(name = "loyalty_points")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyPoints {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_email", nullable = false, unique = true)
    private String accountEmail;

    @Builder.Default
    @Column(name = "total_points", nullable = false)
    private Integer totalPoints = 0;

    @Builder.Default
    @Column(name = "lifetime_points", nullable = false)
    private Integer lifetimePoints = 0;

    @Column(name = "last_earned_at")
    private LocalDateTime lastEarnedAt;
}

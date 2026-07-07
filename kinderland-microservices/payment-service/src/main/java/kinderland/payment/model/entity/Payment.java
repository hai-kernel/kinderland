package kinderland.payment.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tham chiếu đơn hàng = orderId (KHÔNG FK sang order-service). 1 đơn = 1 payment. */
    @Column(nullable = false, unique = true)
    private Long orderId;

    /** Chủ đơn = email (subject JWT). Denormalize để không phải hỏi ngược order-service. */
    @Column(nullable = false)
    private String accountEmail;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    /** Mã giao dịch do VNPay trả về (vnp_TransactionNo). */
    private String transactionCode;

    private LocalDateTime paidAt;

    private LocalDateTime createdAt;
}

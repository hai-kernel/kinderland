package kinderland.payment.repository;

import kinderland.payment.model.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    boolean existsByOrderId(Long orderId);
    Optional<Payment> findByTransactionCode(String transactionCode);
}

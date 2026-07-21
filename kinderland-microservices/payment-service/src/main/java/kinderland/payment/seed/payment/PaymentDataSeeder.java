package kinderland.payment.seed.payment;

import kinderland.common.seed.AbstractDataSeeder;
import kinderland.payment.model.entity.Payment;
import kinderland.payment.model.entity.PaymentMethod;
import kinderland.payment.model.entity.PaymentStatus;
import kinderland.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Order(10)
@ConditionalOnProperty(name = "app.seed.payment.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class PaymentDataSeeder extends AbstractDataSeeder {

    private final PaymentRepository paymentRepository;

    @Override
    public String getName() {
        return "PaymentDataSeeder";
    }

    @Override
    @Transactional
    public void seed() {
        logStart();

        long currentCount = paymentRepository.count();
        if (currentCount >= 60) {
            log.info("Already have {} payments. Skipping payment seed.", currentCount);
            logCompleted(0, 60);
            return;
        }

        int targetPayments = 60;
        int created = 0;
        int skipped = 0;

        LocalDateTime now = LocalDateTime.now();

        for (int i = 1; i <= targetPayments; i++) {
            long orderId = i;
            String email = String.format("customer%02d@kinderland.vn", (i % 30) + 1);

            // Using existsByOrderId to ensure idempotency
            if (paymentRepository.existsByOrderId(orderId)) {
                skipped++;
                continue;
            }

            LocalDateTime createdAt = now.minusDays(i * 3L).minusHours(i);
            
            BigDecimal amount = new BigDecimal(199000 + (i * 10000)).multiply(BigDecimal.valueOf((i % 3) + 1));
            
            PaymentMethod method = (i % 3 == 0) ? PaymentMethod.COD : PaymentMethod.VNPAY;
            PaymentStatus status = (i <= 10) ? PaymentStatus.PENDING : ((i % 15 == 0) ? PaymentStatus.FAILED : PaymentStatus.SUCCESS);
            
            String txnCode = null;
            LocalDateTime paidAt = null;
            
            if (status == PaymentStatus.SUCCESS && method == PaymentMethod.VNPAY) {
                txnCode = "VNP" + System.currentTimeMillis() + i;
                paidAt = createdAt.plusMinutes(5);
            } else if (status == PaymentStatus.SUCCESS && method == PaymentMethod.COD) {
                paidAt = createdAt.plusDays(3);
            }

            Payment payment = Payment.builder()
                    .orderId(orderId)
                    .accountEmail(email)
                    .amount(amount)
                    .method(method)
                    .status(status)
                    .transactionCode(txnCode)
                    .paidAt(paidAt)
                    .createdAt(createdAt)
                    .build();

            paymentRepository.save(payment);
            created++;
        }

        logCompleted(created, skipped);
    }
}

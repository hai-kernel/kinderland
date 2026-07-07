package kinderland.order.event;

import kinderland.order.idempotency.IdempotencyService;
import kinderland.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * Kafka consumer: nhận PaymentCompletedEvent từ payment-service (topic 'payment-events')
 * và chuyển đơn sang PAID. Bean 'paymentCompleted' ⇒ binding 'paymentCompleted-in-0'.
 *
 * Cố ý KHÔNG ném lại exception (chỉ log) để tránh poison-message lặp vô hạn; set PAID là idempotent.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class PaymentCompletedConsumer {

    private final OrderService orderService;
    private final IdempotencyService idempotency;

    @Bean
    public Consumer<PaymentCompletedEvent> paymentCompleted() {
        return event -> {
            log.info("Nhận PaymentCompletedEvent orderId={} → set PAID", event.getOrderId());
            try {
                idempotency.runOnce("payment-completed:" + event.getOrderId(),
                        () -> orderService.markPaid(event.getOrderId()));
            } catch (Exception e) {
                log.error("Set PAID thất bại cho orderId={}: {}", event.getOrderId(), e.getMessage());
            }
        };
    }
}

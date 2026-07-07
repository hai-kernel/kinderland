package kinderland.payment.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

/**
 * Publish PaymentCompletedEvent lên Kafka qua Spring Cloud Stream.
 * Binding 'paymentCompleted-out-0' -> topic 'payment-events' (xem application.yml).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    public static final String PAYMENT_COMPLETED_BINDING = "paymentCompleted-out-0";

    private final StreamBridge streamBridge;

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        boolean sent = streamBridge.send(PAYMENT_COMPLETED_BINDING, event);
        if (sent) {
            log.info("Đã publish PaymentCompletedEvent orderId={}", event.getOrderId());
        } else {
            log.warn("Publish PaymentCompletedEvent THẤT BẠI orderId={}", event.getOrderId());
        }
    }
}

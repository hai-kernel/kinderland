package kinderland.payment.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Phát lên Kafka topic 'payment-events' khi thanh toán thành công.
 * order-service consume để set đơn sang PAID.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent {
    private Long orderId;
    private String accountEmail;
    private String transactionCode;
}

package kinderland.order.event;

import lombok.Data;

/**
 * Bản sao event do payment-service phát lên topic 'payment-events'.
 * Field khớp để Spring Cloud Stream deserialize JSON (không share class giữa service).
 */
@Data
public class PaymentCompletedEvent {
    private Long orderId;
    private String accountEmail;
    private String transactionCode;
}

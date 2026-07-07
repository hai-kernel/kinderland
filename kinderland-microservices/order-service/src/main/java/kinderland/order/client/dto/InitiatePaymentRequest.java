package kinderland.order.client.dto;

import kinderland.order.model.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Body gửi sang payment-service /internal/payments/initiate (khớp DTO bên đó, JSON theo field). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiatePaymentRequest {
    private Long orderId;
    private String accountEmail;
    private BigDecimal amount;
    private PaymentMethod method;
    private String ipAddress;
}

package kinderland.payment.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private String accountEmail;
    private BigDecimal amount;
    private String status;
    private String method;
    private String transactionCode;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}

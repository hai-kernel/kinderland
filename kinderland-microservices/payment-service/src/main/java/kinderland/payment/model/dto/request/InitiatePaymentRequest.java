package kinderland.payment.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kinderland.payment.model.entity.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Body cho POST /internal/payments/initiate — do order-service gọi qua Feign khi checkout.
 * (Internal API, KHÔNG route qua Gateway.)
 */
@Data
public class InitiatePaymentRequest {
    @NotNull
    private Long orderId;
    @NotNull
    private String accountEmail;
    @NotNull
    @Positive
    private BigDecimal amount;
    @NotNull
    private PaymentMethod method;
    /** IP client (order-service lấy từ request rồi truyền xuống — VNPay cần vnp_IpAddr). */
    private String ipAddress;
}

package kinderland.order.model.dto.request;

import jakarta.validation.constraints.NotNull;
import kinderland.order.model.entity.PaymentMethod;
import lombok.Data;

/** Body cho POST /orders/{id}/checkout: khách chọn phương thức thanh toán. */
@Data
public class CheckoutRequest {
    @NotNull
    private PaymentMethod method;
}

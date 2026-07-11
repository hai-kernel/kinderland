package kinderland.order.model.dto.request;

import jakarta.validation.constraints.NotNull;
import kinderland.order.model.entity.PaymentMethod;
import lombok.Data;

/** Body cho POST /orders/{id}/checkout (khớp FE): phương thức thanh toán + điểm muốn dùng. */
@Data
public class CheckoutRequest {
    @NotNull
    private PaymentMethod paymentMethod;
    /** Điểm loyalty muốn dùng để giảm giá lúc checkout (1 điểm = 1 VND). */
    private Integer pointsToUse;
}

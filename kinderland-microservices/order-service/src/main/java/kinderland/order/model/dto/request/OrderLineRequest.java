package kinderland.order.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderLineRequest {
    @NotNull
    private Long productId;
    @Positive
    private int quantity;
}

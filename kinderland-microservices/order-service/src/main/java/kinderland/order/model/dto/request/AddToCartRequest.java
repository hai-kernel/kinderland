package kinderland.order.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/** Body cho POST /cart/add — thêm 1 SKU (tại 1 store) vào giỏ. */
@Data
public class AddToCartRequest {
    @NotNull
    private Long skuId;
    @Positive
    private int quantity;
    @NotNull
    private Long storeId;
}

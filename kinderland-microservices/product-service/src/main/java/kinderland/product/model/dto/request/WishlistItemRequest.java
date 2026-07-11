package kinderland.product.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WishlistItemRequest {
    @NotNull
    private Long productId;
}

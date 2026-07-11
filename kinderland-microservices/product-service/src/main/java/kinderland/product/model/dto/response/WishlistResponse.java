package kinderland.product.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WishlistResponse {
    private String accountEmail;
    private List<WishlistItemResponse> items;
}

package kinderland.order.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CartResponse {
    private Long id;
    private String accountEmail;
    private List<CartItemResponse> items;
}

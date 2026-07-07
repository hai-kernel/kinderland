package kinderland.order.model.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.Data;

/** Body cho PUT /cart/items/{productId}: đặt LẠI số lượng (giá trị tuyệt đối, không cộng dồn). */
@Data
public class UpdateCartItemRequest {
    @Positive
    private int quantity;
}

package kinderland.order.model.dto.request;

import jakarta.validation.constraints.NotNull;
import kinderland.order.model.entity.OrderStatus;
import lombok.Data;

/** Body cho PATCH /orders/{id}: ADMIN chuyển trạng thái đơn. FE gửi field 'orderStatus'. */
@Data
public class UpdateOrderStatusRequest {
    @NotNull
    private OrderStatus orderStatus;
}

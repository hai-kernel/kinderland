package kinderland.order.model.dto.request;

import jakarta.validation.constraints.NotNull;
import kinderland.order.model.entity.OrderStatus;
import lombok.Data;

/** Body cho PATCH /orders/{id}: ADMIN chuyển trạng thái đơn (PAID/SHIPPING/COMPLETED...). */
@Data
public class UpdateOrderStatusRequest {
    @NotNull
    private OrderStatus status;
}

package kinderland.order.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import kinderland.order.model.dto.response.OrderItemResponse;
import kinderland.order.model.dto.response.OrderResponse;
import kinderland.order.model.entity.Order;
import kinderland.order.model.entity.OrderItem;

/**
 * Order -> OrderResponse.
 * - status (enum OrderStatus) -> String: MapStruct map tự động sang tên hằng.
 * - items: List<OrderItem> -> List<OrderItemResponse> qua toItemResponse (MapStruct tự dùng).
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrderMapper {

    OrderResponse toResponse(Order order);

    OrderItemResponse toItemResponse(OrderItem item);
}

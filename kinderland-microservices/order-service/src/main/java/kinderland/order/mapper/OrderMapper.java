package kinderland.order.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import kinderland.order.model.dto.response.OrderItemResponse;
import kinderland.order.model.dto.response.OrderResponse;
import kinderland.order.model.entity.Order;
import kinderland.order.model.entity.OrderItem;

/**
 * Order -> OrderResponse. Cung cấp cả tên field cũ (id/status/lineTotal) LẪN alias FE mong đợi
 * (orderId/orderStatus/orderItemId/totalPrice/customer) để khớp mọi trang FE.
 * - status (enum OrderStatus) -> String: MapStruct map tự động sang tên hằng.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrderMapper {

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "orderStatus", source = "status")
    @Mapping(target = "customer.email", source = "accountEmail")
    @Mapping(target = "customer.fullName", source = "accountEmail")
    @Mapping(target = "customer.id", ignore = true)
    @Mapping(target = "shippingAddress", ignore = true)   // địa chỉ dạng chuỗi thuộc auth-service → null
    @Mapping(target = "shippingCode", ignore = true)
    OrderResponse toResponse(Order order);

    @Mapping(target = "orderItemId", source = "id")
    @Mapping(target = "totalPrice", source = "lineTotal")
    OrderItemResponse toItemResponse(OrderItem item);
}

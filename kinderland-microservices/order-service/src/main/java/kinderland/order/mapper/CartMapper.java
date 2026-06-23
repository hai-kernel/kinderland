package kinderland.order.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import kinderland.order.model.dto.response.CartItemResponse;
import kinderland.order.model.dto.response.CartResponse;
import kinderland.order.model.entity.Cart;
import kinderland.order.model.entity.CartItem;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CartMapper {

    CartResponse toResponse(Cart cart);

    CartItemResponse toItemResponse(CartItem item);
}

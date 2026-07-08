package kinderland.order.model.dto.request;

import lombok.Data;

import java.util.List;

/**
 * Body cho POST /orders/from-cart.
 * productIds = danh sách sản phẩm được TICK trong giỏ để đặt. Bỏ trống/null = đặt TOÀN BỘ giỏ.
 */
@Data
public class FromCartRequest {
    private List<Long> productIds;
}

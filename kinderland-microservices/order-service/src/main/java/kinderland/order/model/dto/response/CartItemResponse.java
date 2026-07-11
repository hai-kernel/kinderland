package kinderland.order.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CartItemResponse {
    private Long id;          // cartItemId (FE dùng để update/remove)
    private Long skuId;
    private Long storeId;
    private String skuCode;
    private String productName;
    private String imageUrl;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal lineTotal;
}

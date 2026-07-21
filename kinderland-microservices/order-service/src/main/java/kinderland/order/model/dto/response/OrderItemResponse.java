package kinderland.order.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    /** id của OrderItem — FE cần để tạo yêu cầu trả hàng (returnApi.orderItemId). */
    private Long id;
    /** Alias khớp FE orderTypes.OrderItemDTO (đọc orderItemId/totalPrice/productId/size/color). */
    private Long orderItemId;
    private Long skuId;
    private Long productId;
    private String skuCode;
    private String size;
    private String color;
    private String productName;
    private String imageUrl;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineTotal;
    private BigDecimal totalPrice;
}

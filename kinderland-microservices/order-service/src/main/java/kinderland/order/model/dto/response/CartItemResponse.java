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
    // Alias khớp FE Cart.tsx (đọc unitPrice/finalPrice/totalPrice/discountAmount). Hiện chưa áp
    // promotion vào giỏ (apply-to-order deferred) nên finalPrice = unitPrice, discountAmount = 0.
    private BigDecimal unitPrice;
    private BigDecimal finalPrice;
    private BigDecimal totalPrice;
    private BigDecimal discountAmount;
}

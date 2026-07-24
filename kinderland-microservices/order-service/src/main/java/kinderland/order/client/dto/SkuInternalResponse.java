package kinderland.order.client.dto;

import lombok.Data;

import java.math.BigDecimal;

/** Bản sao payload Internal API SKU của Product Service (giá + tồn tại 1 store). */
@Data
public class SkuInternalResponse {
    private Long skuId;
    private String skuCode;
    private String size;
    private String color;
    /** GIÁ SAU KHUYẾN MÃI (product-service đã áp promotion). Order/cart chốt tiền theo field này. */
    private BigDecimal price;
    /** Giá gốc trước khuyến mãi. */
    private BigDecimal originalPrice;
    /** Tiền giảm trên MỘT đơn vị. */
    private BigDecimal discountAmount;
    private BigDecimal discountPercent;
    private Long promotionId;
    private String promotionTitle;
    private Long productId;
    private String productName;
    private String imageUrl;
    private Integer availableQuantity;
}

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
    private BigDecimal price;
    private Long productId;
    private String productName;
    private String imageUrl;
    private Integer availableQuantity;
}

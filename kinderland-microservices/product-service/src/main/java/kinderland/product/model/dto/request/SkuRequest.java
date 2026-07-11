package kinderland.product.model.dto.request;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Body tạo/sửa SKU.
 *  - Tạo: FE gửi productId + size/color/type/price (skuCode tự sinh).
 *  - Sửa: FE gửi size/color/type/price (+ skuCode tuỳ chọn), không gửi productId.
 */
@Data
public class SkuRequest {
    private Long productId;
    private String skuCode;
    private String size;
    private String color;
    private String type;
    private BigDecimal price;
}

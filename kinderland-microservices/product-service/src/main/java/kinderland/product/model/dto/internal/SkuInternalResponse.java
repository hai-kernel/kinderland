package kinderland.product.model.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payload Internal API trả Order Service (Feign): giá SKU + tồn khả dụng TẠI 1 store,
 * đủ để order kiểm tra & chốt snapshot khi tạo đơn.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

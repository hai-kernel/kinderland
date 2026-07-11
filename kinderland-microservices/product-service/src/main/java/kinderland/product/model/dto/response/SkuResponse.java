package kinderland.product.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SkuResponse {
    private Long id;
    private String skuCode;
    private String size;
    private String color;
    private String type;
    private String imageUrl;   // presigned URL ảnh SKU (nếu có)
    private BigDecimal price;
    private Long productId;
    private String productName;
}

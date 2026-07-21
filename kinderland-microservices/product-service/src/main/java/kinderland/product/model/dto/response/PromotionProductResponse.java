package kinderland.product.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Sản phẩm nằm trong 1 promotion — khớp FE promotionApi.PromotionProduct
 * (id, name, description, minPrice, imageUrl, categoryName, brandName, promotion:string).
 * KHÔNG dùng @JsonInclude(NON_NULL) để imageUrl luôn có mặt (FE typed string).
 */
@Data
@Builder
public class PromotionProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal minPrice;
    private String imageUrl;
    private String categoryName;
    private String brandName;
    /** Nhãn promotion hiển thị trên FE (dùng title của promotion). */
    private String promotion;
}

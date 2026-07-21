package kinderland.product.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private String ageRange;
    private String gender;
    private BigDecimal price;
    /** Alias của price cho FE (FE đọc field 'minPrice' — mô hình SKU của mono). */
    private BigDecimal minPrice;
    private Integer stockQuantity;
    private boolean active;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    /** Xuất xứ thương hiệu (FE ProductDetail đọc brandOrigin). */
    private String brandOrigin;
    /** Presigned URL ảnh bìa (resolve từ S3 key). */
    private String imageUrl;
    /** Promotion đang áp (FE đọc promotion.discountPercent); null nếu không có. */
    private ProductPromotionInfo promotion;
}

package kinderland.product.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Thông tin promotion rút gọn gắn trên mỗi Product (FE productApi.Product.promotion
 * chỉ đọc discountPercent). Null khi sản phẩm không có promotion.
 */
@Data
@Builder
public class ProductPromotionInfo {
    private Long promotionId;
    private String code;
    private BigDecimal discountPercent;
}

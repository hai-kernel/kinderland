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
    /** GIÁ SAU KHUYẾN MÃI (đã áp promotion của sản phẩm nếu còn hiệu lực). Là giá chốt đơn. */
    private BigDecimal price;
    /** Giá gốc trước khuyến mãi — để giỏ/đơn hiển thị gạch ngang & lưu snapshot. */
    private BigDecimal originalPrice;
    /** Số tiền được giảm trên MỘT đơn vị (originalPrice - price). 0 nếu không có khuyến mãi. */
    private BigDecimal discountAmount;
    /** % giảm + thông tin promotion đang áp (null nếu không có), để đơn lưu snapshot. */
    private BigDecimal discountPercent;
    private Long promotionId;
    private String promotionTitle;
    private Long productId;
    private String productName;
    private String imageUrl;
    private Integer availableQuantity;
}

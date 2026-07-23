package kinderland.product.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Thông tin promotion rút gọn gắn trên mỗi Product. Null khi sản phẩm không có promotion.
 *
 * startDate/endDate BẮT BUỘC có: trang /discounts phải tự lọc khuyến mãi còn hiệu lực
 * (không có cột status trong DB — "đang chạy" = now nằm trong khoảng start..end).
 * Thiếu hai field này, FE hiển thị cả khuyến mãi đã hết hạn lẫn chưa bắt đầu.
 */
@Data
@Builder
public class ProductPromotionInfo {
    private Long promotionId;
    private String code;
    private String title;
    private BigDecimal discountPercent;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}

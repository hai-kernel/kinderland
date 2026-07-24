package kinderland.product.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PromotionResponse {
    private Long promotionId;
    private String title;
    private String description;
    private String code;
    private BigDecimal discountPercent;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    /** Trạng thái + hạn mức sử dụng, để màn quản trị thấy được mã còn bao nhiêu lượt. */
    private Boolean active;
    private Integer usageLimit;
    private Integer usedCount;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private List<PromotionProductResponse> products;
}

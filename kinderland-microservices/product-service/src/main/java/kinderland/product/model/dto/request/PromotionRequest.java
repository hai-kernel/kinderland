package kinderland.product.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PromotionRequest {

    @NotBlank(message = "Promotion title is required")
    private String title;

    private String description;

    private String code;

    @NotNull(message = "Discount percent is required")
    @Min(0)
    @Max(100)
    private BigDecimal discountPercent;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    /**
     * Các ràng buộc dưới đây đều OPTIONAL: bỏ trống khi update = GIỮ NGUYÊN giá trị cũ
     * (xem PromotionService.update), bỏ trống khi create = không giới hạn.
     */
    private Boolean active;

    @Min(value = 1, message = "Usage limit must be at least 1")
    private Integer usageLimit;

    @DecimalMin(value = "0", message = "Min order amount must not be negative")
    private BigDecimal minOrderAmount;

    @DecimalMin(value = "0", message = "Max discount amount must not be negative")
    private BigDecimal maxDiscountAmount;
}

package kinderland.order.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Bản sao phía order-service của kết quả validate mã khuyến mãi từ product-service. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PromotionValidationResponse {
    private boolean valid;
    private String message;
    private Long promotionId;
    private String code;
    private String title;
    private BigDecimal discountPercent;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
}

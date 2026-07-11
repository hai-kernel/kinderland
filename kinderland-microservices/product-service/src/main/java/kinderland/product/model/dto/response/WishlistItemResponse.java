package kinderland.product.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class WishlistItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal price;
    private LocalDateTime addedAt;
}

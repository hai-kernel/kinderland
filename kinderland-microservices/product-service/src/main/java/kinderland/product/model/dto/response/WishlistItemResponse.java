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
    /** Ảnh bìa sản phẩm, đã resolve thành URL xem được (presigned nếu là S3 key). */
    private String imageUrl;
    private BigDecimal price;
    private LocalDateTime addedAt;
}

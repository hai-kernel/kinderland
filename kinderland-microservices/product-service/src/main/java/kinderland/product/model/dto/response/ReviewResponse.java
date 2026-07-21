package kinderland.product.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** Khớp FE reviewApi.Review (accountId để null — product-service chỉ giữ email). */
@Data
@Builder
public class ReviewResponse {
    private Long id;
    private Long accountId;          // null: id số của account thuộc auth-service, không replicate
    private String reviewerName;     // = email (tên hiển thị thuộc auth-service)
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    // SKU info
    private Long skuId;
    private String skuCode;
    private String size;
    private String color;

    // Product info
    private Long productId;
    private String productName;

    // Manager reply
    private String managerReply;
    private LocalDateTime managerReplyAt;
}

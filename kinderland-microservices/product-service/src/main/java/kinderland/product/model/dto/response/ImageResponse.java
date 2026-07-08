package kinderland.product.model.dto.response;

import lombok.Builder;
import lombok.Data;

/** Trả về cho client: url là presigned URL (link ký tạm) để xem ảnh. */
@Data
@Builder
public class ImageResponse {
    private Long id;
    private String key;
    private String url;
    private String fileName;
    private String entityType;
    private Long entityId;
}

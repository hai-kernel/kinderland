package kinderland.product.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BrandResponse {
    private Long id;
    private String name;
    private String origin;

    /** Presigned URL của logo (service resolve từ S3 key); null nếu chưa có logo. */
    private String logoUrl;
}

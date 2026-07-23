package kinderland.product.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BrandRequest {
    @NotBlank(message = "Tên thương hiệu không được rỗng")
    private String name;
    private String origin;

    /** S3 key của logo (FE upload qua POST /api/v1/images rồi gửi key về đây). */
    private String logoUrl;
}

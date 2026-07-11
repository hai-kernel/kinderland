package kinderland.product.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BrandRequest {
    @NotBlank(message = "Tên thương hiệu không được rỗng")
    private String name;
    private String origin;
}

package kinderland.product.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BlogCategoryRequest {
    @NotBlank(message = "Tên danh mục blog không được rỗng")
    private String name;
}

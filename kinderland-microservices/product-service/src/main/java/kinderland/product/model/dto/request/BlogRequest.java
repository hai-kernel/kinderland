package kinderland.product.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BlogRequest {
    @NotBlank(message = "Tiêu đề không được rỗng")
    private String title;
    private String content;
    private Long categoryId;
    /** true = xuất bản ngay khi tạo/sửa. */
    private boolean status;
    private int timeRead;
}

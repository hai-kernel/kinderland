package kinderland.product.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotBlank(message = "Tên sản phẩm không được rỗng")
    private String name;
    private String description;
    private String ageRange;
    private String gender;

    // FE (theo mô hình SKU) KHÔNG gửi price/stock khi tạo product -> để optional, mặc định 0 nếu thiếu.
    // (Khi port SKU, giá/tồn sẽ về đúng chỗ của nó ở SKU.)
    @PositiveOrZero(message = "Giá phải >= 0")
    private BigDecimal price;

    @PositiveOrZero(message = "Tồn kho phải >= 0")
    private Integer stockQuantity;

    private Long categoryId;
    private Long brandId;

    /** S3 key của ảnh bìa (FE upload ảnh trước rồi truyền key về đây). */
    private String imageUrl;
}

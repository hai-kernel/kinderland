package kinderland.product.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Giá không được null")
    @PositiveOrZero(message = "Giá phải >= 0")
    private BigDecimal price;

    @NotNull(message = "Tồn kho không được null")
    @PositiveOrZero(message = "Tồn kho phải >= 0")
    private Integer stockQuantity;

    private Long categoryId;
}

package kinderland.product.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private String ageRange;
    private String gender;
    private BigDecimal price;
    private Integer stockQuantity;
    private boolean active;
    private Long categoryId;
    private String categoryName;
}

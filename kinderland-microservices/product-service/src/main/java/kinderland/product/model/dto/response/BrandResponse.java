package kinderland.product.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BrandResponse {
    private Long id;
    private String name;
    private String origin;
}

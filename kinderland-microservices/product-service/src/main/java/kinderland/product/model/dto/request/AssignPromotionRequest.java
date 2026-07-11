package kinderland.product.model.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class AssignPromotionRequest {
    private List<Long> productIds;
}

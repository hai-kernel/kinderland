package kinderland.order.model.dto.response;

import lombok.Builder;
import lombok.Data;

/** Khớp FE loyaltyApi.LoyaltyPoints {totalPoints, lifetimePoints}. */
@Data
@Builder
public class LoyaltyPointsResponse {
    private Integer totalPoints;
    private Integer lifetimePoints;
}

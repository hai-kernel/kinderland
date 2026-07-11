package kinderland.product.model.dto.response;

import lombok.Builder;
import lombok.Data;

/** Cửa hàng gần + khoảng cách (km) tới toạ độ người dùng. */
@Data
@Builder
public class NearbyStoreResponse {
    private Long id;
    private String name;
    private String code;
    private String address;
    private String phone;
    private Double latitude;
    private Double longitude;
    private Double distanceKm;
}

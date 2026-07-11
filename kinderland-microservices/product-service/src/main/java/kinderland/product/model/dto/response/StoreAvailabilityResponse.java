package kinderland.product.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

/** Tình trạng còn hàng của 1 SKU tại từng cửa hàng (GET /inventory/availability?skuId=). */
@Data
@Builder
public class StoreAvailabilityResponse {
    private Long storeId;
    private String storeName;
    private String address;
    private Double latitude;
    private Double longitude;
    private String phone;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private String availabilityStatus;   // IN_STOCK / OUT_OF_STOCK
    private Integer quantity;
}

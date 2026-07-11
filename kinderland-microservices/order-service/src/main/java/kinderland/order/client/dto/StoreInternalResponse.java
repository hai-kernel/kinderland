package kinderland.order.client.dto;

import lombok.Data;

/** Thông tin store nhận từ product-service (Feign) để enrich return + shipping label. */
@Data
public class StoreInternalResponse {
    private Long id;
    private String name;
    private String address;
    private String phone;
}

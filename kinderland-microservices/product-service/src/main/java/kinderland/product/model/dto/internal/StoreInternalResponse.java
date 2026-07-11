package kinderland.product.model.dto.internal;

import lombok.Builder;
import lombok.Data;

/** Thông tin store tối thiểu cho order-service (Feign) — dùng khi hiển thị return + shipping label. */
@Data
@Builder
public class StoreInternalResponse {
    private Long id;
    private String name;
    private String address;
    private String phone;
}

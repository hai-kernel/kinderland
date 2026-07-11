package kinderland.product.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Body cho import/adjust/dispose — store lấy từ manager đang đăng nhập (không gửi trong body). */
@Data
public class InventoryRequest {
    @NotNull
    private Long skuId;
    @NotNull
    private Integer quantity;
}

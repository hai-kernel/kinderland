package kinderland.product.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/** Body tạo phiếu chuyển kho nháp (khớp FE: {toStoreId, skuId, quantity}). */
@Data
public class TransferCreateRequestDTO {

    @NotNull(message = "toStoreId is required")
    private Long toStoreId;

    @NotNull(message = "skuId is required")
    private Long skuId;

    @NotNull(message = "quantity is required")
    @Positive(message = "quantity must be positive")
    private Integer quantity;
}

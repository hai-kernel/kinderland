package kinderland.order.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/** 1 dòng đặt hàng: SKU + số lượng (storeId chung cho cả đơn nằm ở query param). */
@Data
public class OrderLineRequest {
    @NotNull
    private Long skuId;
    @Positive
    private int quantity;
}

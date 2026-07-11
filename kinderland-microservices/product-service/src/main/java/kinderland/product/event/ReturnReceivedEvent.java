package kinderland.product.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Nhận từ Kafka topic 'return-received-events' (order-service publish khi nhận hàng trả).
 * product-service CỘNG kho lại theo (sku, store).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnReceivedEvent {
    private Long returnId;
    private Long skuId;
    private Long storeId;
    private int quantity;
}

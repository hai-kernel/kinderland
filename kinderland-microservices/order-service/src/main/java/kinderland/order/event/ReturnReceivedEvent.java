package kinderland.order.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Phát lên Kafka topic 'return-received-events' khi quản lý xác nhận đã nhận hàng trả.
 * product-service consume để CỘNG kho lại theo (sku, store). Idempotency theo returnId.
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

package kinderland.order.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Phát lên Kafka topic 'order-events' sau khi đơn được tạo.
 * product-service consume để trừ kho (Saga bất đồng bộ).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private Long orderId;
    private String accountEmail;
    private List<Item> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long skuId;
        private Long storeId;
        private int quantity;
    }
}

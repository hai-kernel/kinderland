package kinderland.order.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Phát lên Kafka topic 'order-cancelled-events' khi đơn bị huỷ.
 * product-service consume để CỘNG kho lại (bù cho lần trừ kho lúc tạo đơn).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledEvent {
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

package kinderland.product.event;

import lombok.Data;

import java.util.List;

/**
 * Bản sao event do order-service phát lên topic 'order-events'.
 * Field khớp để Spring Cloud Stream deserialize JSON. (Không share class giữa service.)
 */
@Data
public class OrderCreatedEvent {
    private Long orderId;
    private String accountEmail;
    private List<Item> items;

    @Data
    public static class Item {
        private Long skuId;
        private Long storeId;
        private int quantity;
    }
}

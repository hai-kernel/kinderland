package kinderland.product.event;

import lombok.Data;

import java.util.List;

/**
 * Bản sao event do order-service phát lên topic 'order-cancelled-events'.
 * Field khớp để Spring Cloud Stream deserialize JSON (không share class giữa service).
 */
@Data
public class OrderCancelledEvent {
    private Long orderId;
    private String accountEmail;
    private List<Item> items;

    @Data
    public static class Item {
        private Long productId;
        private int quantity;
    }
}

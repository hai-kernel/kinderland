package kinderland.product.event;

import kinderland.product.idempotency.IdempotencyService;
import kinderland.product.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * Kafka consumer HOÀN KHO khi đơn bị huỷ.
 * Bean 'orderCancelled' ⇒ binding 'orderCancelled-in-0' (xem application.yml), topic 'order-cancelled-events'.
 *
 * Cố ý KHÔNG ném lại exception (chỉ log) để tránh poison-message lặp vô hạn ở Kafka.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class OrderCancelledConsumer {

    private final InventoryService inventoryService;
    private final IdempotencyService idempotency;

    @Bean
    public Consumer<OrderCancelledEvent> orderCancelled() {
        return event -> {
            log.info("Nhận OrderCancelledEvent orderId={} ({} dòng) → hoàn kho theo (sku,store)",
                    event.getOrderId(), event.getItems() == null ? 0 : event.getItems().size());
            if (event.getItems() == null) {
                return;
            }
            try {
                // Hoàn kho cả đơn trong 1 transaction + chống hoàn trùng (Kafka at-least-once).
                idempotency.runOnce("order-cancelled:" + event.getOrderId(), () ->
                        event.getItems().forEach(item ->
                                inventoryService.restockStock(item.getSkuId(), item.getStoreId(), item.getQuantity())));
            } catch (Exception e) {
                log.error("Hoàn kho thất bại (orderId={}): {}", event.getOrderId(), e.getMessage());
            }
        };
    }
}

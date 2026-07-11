package kinderland.product.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import kinderland.product.idempotency.IdempotencyService;
import kinderland.product.service.InventoryService;

import java.util.function.Consumer;

/**
 * Kafka consumer trừ kho theo mô hình Saga bất đồng bộ.
 *
 * Bean 'orderCreated' ⇒ binding 'orderCreated-in-0' (xem application.yml), gắn topic 'order-events'.
 * Khi order-service tạo đơn xong và bắn OrderCreatedEvent, consumer này trừ kho từng sản phẩm.
 *
 * Cố ý KHÔNG ném lại exception (chỉ log) để tránh poison-message lặp vô hạn ở Kafka;
 * tồn kho đã được Feign kiểm tra lúc tạo đơn nên trường hợp thiếu kho ở đây là hiếm (race).
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class OrderStockConsumer {

    private final InventoryService inventoryService;
    private final IdempotencyService idempotency;

    @Bean
    public Consumer<OrderCreatedEvent> orderCreated() {
        return event -> {
            log.info("Nhận OrderCreatedEvent orderId={} ({} dòng) → trừ kho theo (sku,store)",
                    event.getOrderId(), event.getItems() == null ? 0 : event.getItems().size());
            if (event.getItems() == null) {
                return;
            }
            try {
                // Trừ kho cả đơn trong 1 transaction + chống xử lý trùng (Kafka at-least-once).
                idempotency.runOnce("order-created:" + event.getOrderId(), () ->
                        event.getItems().forEach(item ->
                                inventoryService.decrementStock(item.getSkuId(), item.getStoreId(), item.getQuantity())));
            } catch (Exception e) {
                log.error("Trừ kho thất bại (orderId={}): {}", event.getOrderId(), e.getMessage());
            }
        };
    }
}

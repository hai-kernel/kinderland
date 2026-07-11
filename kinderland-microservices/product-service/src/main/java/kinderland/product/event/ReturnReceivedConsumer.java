package kinderland.product.event;

import kinderland.product.idempotency.IdempotencyService;
import kinderland.product.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * Kafka consumer HOÀN KHO khi nhận hàng trả.
 * Bean 'returnReceived' ⇒ binding 'returnReceived-in-0', topic 'return-received-events'.
 * Idempotency theo returnId để Kafka at-least-once không cộng kho trùng.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ReturnReceivedConsumer {

    private final InventoryService inventoryService;
    private final IdempotencyService idempotency;

    @Bean
    public Consumer<ReturnReceivedEvent> returnReceived() {
        return event -> {
            log.info("Nhận ReturnReceivedEvent returnId={} (sku={}, store={}, qty={}) → hoàn kho",
                    event.getReturnId(), event.getSkuId(), event.getStoreId(), event.getQuantity());
            try {
                idempotency.runOnce("return-received:" + event.getReturnId(), () ->
                        inventoryService.restockStock(event.getSkuId(), event.getStoreId(), event.getQuantity()));
            } catch (Exception e) {
                log.error("Hoàn kho (return) thất bại returnId={}: {}", event.getReturnId(), e.getMessage());
            }
        };
    }
}

package kinderland.product.event;

import kinderland.product.service.ProductService;
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

    private final ProductService productService;

    @Bean
    public Consumer<OrderCancelledEvent> orderCancelled() {
        return event -> {
            log.info("Nhận OrderCancelledEvent orderId={} ({} dòng) → hoàn kho",
                    event.getOrderId(), event.getItems() == null ? 0 : event.getItems().size());
            if (event.getItems() == null) {
                return;
            }
            event.getItems().forEach(item -> {
                try {
                    productService.restockStock(item.getProductId(), item.getQuantity());
                } catch (Exception e) {
                    log.error("Hoàn kho thất bại cho productId={} (orderId={}): {}",
                            item.getProductId(), event.getOrderId(), e.getMessage());
                }
            });
        };
    }
}

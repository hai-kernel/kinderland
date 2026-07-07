package kinderland.order.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

/**
 * Publish OrderCreatedEvent lên Kafka qua Spring Cloud Stream.
 * Binding 'orderCreated-out-0' -> topic 'order-events' (xem application.yml).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    public static final String ORDER_CREATED_BINDING = "orderCreated-out-0";
    public static final String ORDER_CANCELLED_BINDING = "orderCancelled-out-0";

    private final StreamBridge streamBridge;

    public void publishOrderCreated(OrderCreatedEvent event) {
        boolean sent = streamBridge.send(ORDER_CREATED_BINDING, event);
        if (sent) {
            log.info("Đã publish OrderCreatedEvent orderId={}", event.getOrderId());
        } else {
            log.warn("Publish OrderCreatedEvent THẤT BẠI orderId={}", event.getOrderId());
        }
    }

    public void publishOrderCancelled(OrderCancelledEvent event) {
        boolean sent = streamBridge.send(ORDER_CANCELLED_BINDING, event);
        if (sent) {
            log.info("Đã publish OrderCancelledEvent orderId={}", event.getOrderId());
        } else {
            log.warn("Publish OrderCancelledEvent THẤT BẠI orderId={}", event.getOrderId());
        }
    }
}

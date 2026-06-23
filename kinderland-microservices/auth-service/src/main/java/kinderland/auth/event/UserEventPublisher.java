package kinderland.auth.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

/**
 * Phát event lên Kafka qua Spring Cloud Stream.
 *
 * Binding 'userCreated-out-0' được trỏ tới topic 'notification-events' trong application.yml
 * (spring.cloud.stream.bindings.userCreated-out-0.destination=notification-events).
 *
 * Dùng StreamBridge để publish từ code nghiệp vụ (imperative) thay vì khai báo Supplier.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    public static final String USER_CREATED_BINDING = "userCreated-out-0";

    private final StreamBridge streamBridge;

    public void publishUserCreated(UserCreatedEvent event) {
        boolean sent = streamBridge.send(USER_CREATED_BINDING, event);
        if (sent) {
            log.info("Đã publish UserCreatedEvent cho email={}", event.getEmail());
        } else {
            log.warn("Publish UserCreatedEvent THẤT BẠI cho email={}", event.getEmail());
        }
    }
}

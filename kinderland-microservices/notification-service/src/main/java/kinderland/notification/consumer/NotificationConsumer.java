package kinderland.notification.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import kinderland.notification.event.UserCreatedEvent;
import kinderland.notification.service.EmailNotificationService;

import java.util.function.Consumer;

/**
 * Kafka consumer theo mô hình functional của Spring Cloud Stream.
 *
 * Bean tên 'userRegistered' ⇒ binding 'userRegistered-in-0' (xem application.yml),
 * gắn vào topic 'notification-events'. Spring tự deserialize JSON → UserCreatedEvent.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final EmailNotificationService emailNotificationService;

    @Bean
    public Consumer<UserCreatedEvent> userRegistered() {
        return event -> {
            log.info("Nhận UserCreatedEvent từ topic notification-events: email={}, username={}",
                    event.getEmail(), event.getUsername());
            emailNotificationService.sendWelcomeEmail(event);
        };
    }
}

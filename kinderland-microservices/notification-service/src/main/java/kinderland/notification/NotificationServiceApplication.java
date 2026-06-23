package kinderland.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Notification Service (port 8084) — KHÔNG có database.
 *
 * Lắng nghe Kafka topic 'notification-events' (Spring Cloud Stream) và gửi email.
 * Hiện xử lý UserCreatedEvent (đăng ký thành công) → gửi email chào mừng.
 */
@SpringBootApplication
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}

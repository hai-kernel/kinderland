package kinderland.auth.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event phát ra khi một user đăng ký thành công.
 * Được serialize JSON và gửi lên Kafka topic 'notification-events';
 * notification-service (Bước 4) sẽ lắng nghe và gửi email chào mừng.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String role;
    private String occurredAt;
}

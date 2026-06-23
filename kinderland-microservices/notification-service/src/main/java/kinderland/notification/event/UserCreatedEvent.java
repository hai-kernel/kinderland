package kinderland.notification.event;

import lombok.Data;

/**
 * Bản sao event do auth-service phát. Field khớp để deserialize JSON từ Kafka.
 * (Cố ý không share class giữa các service để tránh coupling biên dịch.)
 */
@Data
public class UserCreatedEvent {
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String role;
    private String occurredAt;
}

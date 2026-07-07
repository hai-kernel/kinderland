package kinderland.order.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Ghi lại các event ĐÃ xử lý để chống xử lý trùng (Kafka giao "at-least-once" — event có thể tới >1 lần).
 * Khoá chính = eventKey (vd "payment-completed:42"): mỗi event nghiệp vụ xử lý đúng 1 lần.
 */
@Entity
@Table(name = "processed_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedEvent {

    @Id
    @Column(length = 200)
    private String eventKey;

    private LocalDateTime processedAt;
}

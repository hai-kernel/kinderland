package kinderland.order.idempotency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Đảm bảo một event chỉ được xử lý MỘT lần.
 *
 * runOnce chạy action + ghi dấu processed trong CÙNG 1 transaction: nếu action lỗi thì rollback cả
 * dấu processed → Kafka giao lại sẽ xử lý lại (an toàn). Nếu action thành công, lần giao trùng sau đó
 * sẽ thấy eventKey đã tồn tại và bỏ qua.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final ProcessedEventRepository repository;

    @Transactional
    public void runOnce(String eventKey, Runnable action) {
        if (repository.existsById(eventKey)) {
            log.info("Bỏ qua event đã xử lý: {}", eventKey);
            return;
        }
        action.run();
        repository.save(new ProcessedEvent(eventKey, LocalDateTime.now()));
    }
}

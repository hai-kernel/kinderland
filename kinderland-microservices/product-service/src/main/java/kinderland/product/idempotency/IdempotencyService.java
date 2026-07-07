package kinderland.product.idempotency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Đảm bảo một event chỉ được xử lý MỘT lần.
 *
 * runOnce chạy action + ghi dấu processed trong CÙNG 1 transaction: action lỗi → rollback cả dấu
 * processed (Kafka giao lại sẽ xử lý lại). Action xong → lần giao trùng sau bỏ qua vì eventKey đã tồn tại.
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

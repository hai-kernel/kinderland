package kinderland.order.scheduler;

import kinderland.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Định kỳ tự huỷ các đơn PENDING quá hạn (khách không thanh toán) để HOÀN KHO —
 * tránh giữ kho vĩnh viễn cho đơn bị bỏ dở. Chu kỳ quét cấu hình qua order.expiry-scan-ms.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExpiryScheduler {

    private final OrderService orderService;

    @Scheduled(fixedDelayString = "${order.expiry-scan-ms:60000}")
    public void cancelExpiredOrders() {
        try {
            orderService.cancelExpiredPendingOrders();
        } catch (Exception e) {
            log.error("Lỗi khi quét đơn PENDING quá hạn: {}", e.getMessage());
        }
    }
}

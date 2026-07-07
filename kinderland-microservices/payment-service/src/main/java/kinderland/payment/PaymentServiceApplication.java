package kinderland.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Payment Service (port 8085) — DB riêng kinderland_payment_db.
 *
 * Xử lý thanh toán VNPay + COD. Được order-service gọi qua Feign (Internal API) để khởi tạo
 * thanh toán; sau khi thanh toán thành công, bắn PaymentCompletedEvent (Kafka) để order-service
 * set đơn sang PAID. KHÔNG có FK sang Order — chỉ giữ orderId (Long) + accountEmail (String).
 */
@SpringBootApplication
public class PaymentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}

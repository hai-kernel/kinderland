package kinderland.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Order Service (port 8083) — DB riêng kinderland_order_db.
 *
 * Quản lý giỏ hàng + đặt hàng. Khi tạo đơn, gọi Product Service qua OpenFeign
 * (Internal API) để lấy GIÁ & TỒN KHO chuẩn, rồi trừ kho.
 *
 * Đã GỠ coupling: KHÔNG còn @ManyToOne Account/Address/Promotion/Store. Thay bằng:
 *  - accountEmail (String) — lấy từ header X-Auth-Email (subject của JWT),
 *  - productId (Long) + field denormalized (productName, unitPrice).
 *
 * @EnableFeignClients để kích hoạt ProductClient.
 */
@SpringBootApplication
@EnableFeignClients
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}

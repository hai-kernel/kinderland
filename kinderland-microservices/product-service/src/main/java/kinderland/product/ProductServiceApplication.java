package kinderland.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Product Service (port 8082) — DB riêng kinderland_product_db.
 *
 * Cung cấp:
 *  - REST API CRUD sản phẩm/danh mục cho Frontend (qua API Gateway).
 *  - Internal API (/internal/**) chỉ dùng nội bộ cho Order Service gọi qua OpenFeign
 *    để lấy giá / tồn kho khi tạo đơn (KHÔNG expose ra ngoài qua Gateway).
 *
 * Đã GỠ coupling: không còn tham chiếu entity Account của auth. Danh tính (nếu cần)
 * đọc từ header Gateway qua GatewayAuthContext.
 */
@SpringBootApplication
public class ProductServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}

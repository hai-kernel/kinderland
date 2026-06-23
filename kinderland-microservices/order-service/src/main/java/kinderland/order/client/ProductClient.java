package kinderland.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import kinderland.order.client.dto.ProductInternalResponse;

/**
 * OpenFeign client gọi Internal API của Product Service (kiểm tra giá/tồn lúc tạo đơn).
 *
 * name = "PRODUCT-SERVICE" -> phân giải qua Eureka + Spring Cloud LoadBalancer
 * (không hard-code host:port). Đi THẲNG tới product-service, KHÔNG qua Gateway.
 *
 * Việc trừ kho KHÔNG dùng Feign nữa — đã chuyển sang event (OrderEventPublisher).
 */
@FeignClient(name = "PRODUCT-SERVICE", path = "/internal/products")
public interface ProductClient {

    @GetMapping("/{id}")
    ProductInternalResponse getProduct(@PathVariable("id") Long id);
}

package kinderland.product.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client gọi Internal API của order-service để kiểm tra khách đã mua SKU (ràng buộc Review).
 * Phân giải qua Eureka (lb://ORDER-SERVICE), KHÔNG qua Gateway.
 */
@FeignClient(name = "ORDER-SERVICE", path = "/internal/orders")
public interface OrderClient {

    @GetMapping("/purchased")
    boolean hasPurchased(@RequestParam("email") String email, @RequestParam("skuId") Long skuId);
}

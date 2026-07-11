package kinderland.order.client;

import kinderland.order.client.dto.StoreInternalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client lấy thông tin store từ product-service (Internal API, qua Eureka, KHÔNG qua Gateway).
 * contextId riêng vì cùng name PRODUCT-SERVICE với ProductClient.
 */
@FeignClient(name = "PRODUCT-SERVICE", contextId = "storeClient", path = "/internal/stores")
public interface StoreClient {

    @GetMapping("/{id}")
    StoreInternalResponse getStore(@PathVariable("id") Long id);
}

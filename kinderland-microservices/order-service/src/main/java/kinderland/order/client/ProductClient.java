package kinderland.order.client;

import kinderland.order.client.dto.SkuInternalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * OpenFeign client gọi Internal API SKU của Product Service: lấy giá SKU + tồn khả dụng TẠI 1 store,
 * dùng khi tạo đơn (đồng bộ). Phân giải qua Eureka (lb://PRODUCT-SERVICE), KHÔNG qua Gateway.
 * Việc TRỪ KHO không dùng Feign — chuyển sang event (OrderCreatedEvent) trừ kho theo (sku,store).
 */
@FeignClient(name = "PRODUCT-SERVICE", path = "/internal/skus")
public interface ProductClient {

    @GetMapping("/{id}")
    SkuInternalResponse getSku(@PathVariable("id") Long id, @RequestParam("storeId") Long storeId);
}

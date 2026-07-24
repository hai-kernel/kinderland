package kinderland.order.client;

import kinderland.order.client.dto.PromotionValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * OpenFeign client gọi Internal API khuyến mãi của Product Service.
 *
 * contextId là BẮT BUỘC ở đây: đã có ProductClient trỏ tới cùng name "PRODUCT-SERVICE",
 * hai @FeignClient trùng name mà không phân biệt contextId sẽ làm context Spring fail khi khởi động.
 */
@FeignClient(name = "PRODUCT-SERVICE", contextId = "promotionClient", path = "/internal/promotions")
public interface PromotionClient {

    /** Kiểm tra mã + tính số tiền giảm trên subtotal do order-service tự cộng từ giá SKU. */
    @GetMapping("/validate")
    PromotionValidationResponse validate(@RequestParam("code") String code,
                                         @RequestParam("subtotal") BigDecimal subtotal);

    /** Ghi nhận đã dùng 1 lượt — chỉ gọi sau khi thanh toán thành công. */
    @PostMapping("/{id}/redeem")
    boolean redeem(@PathVariable("id") Long id);
}

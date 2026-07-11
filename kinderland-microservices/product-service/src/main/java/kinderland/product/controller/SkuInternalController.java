package kinderland.product.controller;

import kinderland.product.model.dto.internal.SkuInternalResponse;
import kinderland.product.service.SkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal API — Order Service gọi qua Feign để lấy giá SKU + tồn khả dụng tại 1 store.
 * KHÔNG route qua Gateway.
 */
@RestController
@RequestMapping("/internal/skus")
@RequiredArgsConstructor
public class SkuInternalController {

    private final SkuService skuService;

    @GetMapping("/{id}")
    public SkuInternalResponse getSku(@PathVariable Long id, @RequestParam Long storeId) {
        return skuService.getInternal(id, storeId);
    }
}

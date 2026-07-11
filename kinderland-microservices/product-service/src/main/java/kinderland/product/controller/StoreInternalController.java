package kinderland.product.controller;

import kinderland.product.model.dto.internal.StoreInternalResponse;
import kinderland.product.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal API — order-service gọi qua Feign để lấy thông tin store (return + shipping label).
 * KHÔNG route qua Gateway.
 */
@RestController
@RequestMapping("/internal/stores")
@RequiredArgsConstructor
public class StoreInternalController {

    private final StoreService storeService;

    @GetMapping("/{id}")
    public StoreInternalResponse getStore(@PathVariable Long id) {
        return storeService.getInternal(id);
    }
}

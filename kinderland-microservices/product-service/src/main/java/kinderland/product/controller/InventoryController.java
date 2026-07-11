package kinderland.product.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kinderland.common.dto.BaseResponse;
import kinderland.product.model.dto.request.InventoryRequest;
import kinderland.product.model.dto.response.InventoryItemResponse;
import kinderland.product.model.dto.response.StoreAvailabilityResponse;
import kinderland.product.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /** Tồn kho của 1 store (storeId tuỳ chọn; bỏ trống = store của manager hiện tại). */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<List<InventoryItemResponse>>> getInventory(
            @RequestParam(value = "storeId", required = false) Long storeId, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK", inventoryService.getInventory(storeId)));
    }

    /** Tình trạng còn hàng của 1 SKU tại các cửa hàng (public — cho khách chọn store). */
    @GetMapping("/availability")
    public ResponseEntity<BaseResponse<List<StoreAvailabilityResponse>>> availability(
            @RequestParam("skuId") Long skuId, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK", inventoryService.getStoreAvailability(skuId)));
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<Void>> importStock(@Valid @RequestBody InventoryRequest request, HttpServletRequest req) {
        inventoryService.importStock(request);
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Đã nhập kho", null));
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<Void>> adjustStock(@Valid @RequestBody InventoryRequest request, HttpServletRequest req) {
        inventoryService.adjustStock(request);
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Đã điều chỉnh kho", null));
    }

    @PostMapping("/dispose")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<Void>> disposeStock(@Valid @RequestBody InventoryRequest request, HttpServletRequest req) {
        inventoryService.disposeStock(request);
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Đã huỷ hàng hỏng", null));
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<Void>> transferStock(@RequestParam Long fromStoreId, @RequestParam Long toStoreId,
                                                            @RequestParam Long skuId, @RequestParam Integer quantity,
                                                            HttpServletRequest req) {
        inventoryService.transferStock(fromStoreId, toStoreId, skuId, quantity);
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Đã chuyển kho", null));
    }
}

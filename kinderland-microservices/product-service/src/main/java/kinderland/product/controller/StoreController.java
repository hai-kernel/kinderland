package kinderland.product.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kinderland.common.dto.BaseResponse;
import kinderland.product.model.dto.request.StoreRequest;
import kinderland.product.model.dto.response.NearbyStoreResponse;
import kinderland.product.model.dto.response.StoreResponse;
import kinderland.product.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<StoreResponse>>> getAll(HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK", storeService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<StoreResponse>> getById(@PathVariable Long id, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK", storeService.getById(id)));
    }

    /** Cửa hàng gần toạ độ người dùng (sắp theo khoảng cách). */
    @GetMapping("/nearby")
    public ResponseEntity<BaseResponse<List<NearbyStoreResponse>>> nearby(
            @RequestParam("lat") double lat, @RequestParam("lng") double lng, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK", storeService.nearby(lat, lng)));
    }

    @GetMapping("/search")
    public ResponseEntity<BaseResponse<List<StoreResponse>>> search(
            @RequestParam("keyword") String keyword, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK", storeService.search(keyword)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<StoreResponse>> create(@Valid @RequestBody StoreRequest request, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.ok(201, req.getRequestURI(), "Created", storeService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<StoreResponse>> update(@PathVariable Long id, @Valid @RequestBody StoreRequest request, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Updated", storeService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id, HttpServletRequest req) {
        storeService.delete(id);
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Deleted", null));
    }
}

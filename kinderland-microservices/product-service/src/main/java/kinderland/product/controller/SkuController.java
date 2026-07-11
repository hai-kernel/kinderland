package kinderland.product.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kinderland.common.dto.BaseResponse;
import kinderland.product.model.dto.request.SkuRequest;
import kinderland.product.model.dto.response.SkuResponse;
import kinderland.product.service.SkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sku")
@RequiredArgsConstructor
public class SkuController {

    private final SkuService skuService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<SkuResponse>>> getAll(@RequestParam(value = "productId", required = false) Long productId,
                                                                  HttpServletRequest req) {
        List<SkuResponse> data = (productId != null) ? skuService.getByProduct(productId) : skuService.getAll();
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<SkuResponse>> getById(@PathVariable Long id, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK", skuService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<SkuResponse>> create(@Valid @RequestBody SkuRequest request, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.ok(201, req.getRequestURI(), "Created", skuService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<SkuResponse>> update(@PathVariable Long id, @Valid @RequestBody SkuRequest request, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Updated", skuService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id, HttpServletRequest req) {
        skuService.delete(id);
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Deleted", null));
    }
}

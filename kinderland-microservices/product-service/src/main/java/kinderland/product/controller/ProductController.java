package kinderland.product.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import kinderland.common.dto.BaseResponse;
import kinderland.product.model.dto.request.ProductRequest;
import kinderland.product.model.dto.response.ProductResponse;
import kinderland.product.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Danh sách sản phẩm. Mặc định BỎ QUA hàng đã xoá mềm (active = false).
     * includeInactive=true: trang admin xem cả hàng đã ẩn để khôi phục.
     */
    @GetMapping
    public ResponseEntity<BaseResponse<List<ProductResponse>>> getAll(
            @RequestParam(defaultValue = "false") boolean includeInactive,
            HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK",
                productService.getAll(includeInactive)));
    }

    /** Duyệt sản phẩm có lọc (public) — khớp FE productApi.browse. */
    @GetMapping("/browse")
    public ResponseEntity<BaseResponse<List<ProductResponse>>> browse(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK",
                productService.browse(keyword, categoryId, brandId, minPrice, maxPrice)));
    }

    /** Tìm sản phẩm theo từ khoá (public) — khớp FE productApi.search. */
    @GetMapping("/search")
    public ResponseEntity<BaseResponse<List<ProductResponse>>> search(
            @RequestParam(required = false) String keyword, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK", productService.search(keyword)));
    }

    /** Chi tiết sản phẩm (public) — khớp FE productApi.getDetail (đọc brandOrigin, promotion...). */
    @GetMapping("/view-detail/{id}")
    public ResponseEntity<BaseResponse<ProductResponse>> viewDetail(@PathVariable Long id, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK", productService.getById(id)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ProductResponse>> getById(@PathVariable Long id, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK", productService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<ProductResponse>> create(@Valid @RequestBody ProductRequest request, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.ok(201, req.getRequestURI(), "Created", productService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<ProductResponse>> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Updated", productService.update(id, request)));
    }

    /**
     * XOÁ MỀM: ẩn sản phẩm (active = false), không xoá dòng khỏi database.
     * Giữ nguyên DELETE /{id} để frontend không phải đổi gì.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id, HttpServletRequest req) {
        productService.delete(id);
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Đã ẩn sản phẩm", null));
    }

    /** Khôi phục sản phẩm đã ẩn. */
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<ProductResponse>> restore(@PathVariable Long id, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Đã khôi phục", productService.restore(id)));
    }
}

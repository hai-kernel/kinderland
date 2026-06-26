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

    @GetMapping
    public ResponseEntity<BaseResponse<List<ProductResponse>>> getAll(HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK", productService.getAll()));
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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id, HttpServletRequest req) {
        productService.delete(id);
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Deleted", null));
    }
}

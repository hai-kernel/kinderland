package kinderland.product.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kinderland.common.dto.BaseResponse;
import kinderland.product.model.dto.request.BrandRequest;
import kinderland.product.model.dto.response.BrandResponse;
import kinderland.product.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<BrandResponse>>> getAll(HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK", brandService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<BrandResponse>> getById(@PathVariable Long id, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK", brandService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<BrandResponse>> create(@Valid @RequestBody BrandRequest request, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.ok(201, req.getRequestURI(), "Created", brandService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<BrandResponse>> update(@PathVariable Long id, @Valid @RequestBody BrandRequest request, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Updated", brandService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id, HttpServletRequest req) {
        brandService.delete(id);
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Deleted", null));
    }
}

package kinderland.product.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import kinderland.common.dto.BaseResponse;
import kinderland.product.model.dto.request.CategoryRequest;
import kinderland.product.model.dto.response.CategoryResponse;
import kinderland.product.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<CategoryResponse>>> getAll(HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK", categoryService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<CategoryResponse>> getById(@PathVariable Long id, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK", categoryService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<CategoryResponse>> create(@Valid @RequestBody CategoryRequest request, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.ok(201, req.getRequestURI(), "Created", categoryService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<CategoryResponse>> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Updated", categoryService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id, HttpServletRequest req) {
        categoryService.delete(id);
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Deleted", null));
    }
}

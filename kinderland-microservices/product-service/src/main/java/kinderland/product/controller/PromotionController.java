package kinderland.product.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kinderland.common.dto.BaseResponse;
import kinderland.product.model.dto.request.AssignPromotionRequest;
import kinderland.product.model.dto.request.PromotionRequest;
import kinderland.product.model.dto.response.PromotionProductResponse;
import kinderland.product.model.dto.response.PromotionResponse;
import kinderland.product.model.dto.response.PromotionValidationResponse;
import kinderland.product.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    public ResponseEntity<BaseResponse<Page<PromotionResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK",
                promotionService.search(keyword, page, size)));
    }

    /**
     * Kiểm tra mã khuyến mãi + trả số tiền được giảm cho một subtotal.
     * FE gọi ngay khi người dùng bấm "Áp dụng" để hiển thị giảm giá thật (không mock).
     *
     * PHẢI khai báo TRƯỚC /{id}: Spring ưu tiên path literal hơn path variable, nhưng để
     * cạnh nhau cho người đọc thấy rõ "validate" không phải là một id.
     * Đây CHỈ là preview để hiển thị — số tiền cuối cùng do order-service tính lại khi tạo đơn.
     */
    @GetMapping("/validate")
    public ResponseEntity<BaseResponse<PromotionValidationResponse>> validate(
            @RequestParam String code,
            @RequestParam(defaultValue = "0") BigDecimal subtotal,
            HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK",
                promotionService.validate(code, subtotal)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<PromotionResponse>> getById(@PathVariable Long id, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK", promotionService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<BaseResponse<PromotionResponse>> create(
            @Valid @RequestBody PromotionRequest request, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.ok(201, req.getRequestURI(), "Created", promotionService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<BaseResponse<PromotionResponse>> update(
            @PathVariable Long id, @Valid @RequestBody PromotionRequest request, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Updated",
                promotionService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id, HttpServletRequest req) {
        promotionService.delete(id);
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Deleted", null));
    }

    @PostMapping("/{id}/assign-products")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<BaseResponse<List<PromotionProductResponse>>> assignProducts(
            @PathVariable("id") Long promotionId,
            @RequestBody AssignPromotionRequest request,
            HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Assigned",
                promotionService.assignProducts(promotionId, request.getProductIds())));
    }
}

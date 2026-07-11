package kinderland.product.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kinderland.common.dto.BaseResponse;
import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.common.security.GatewayAuthContext;
import kinderland.product.model.dto.request.WishlistItemRequest;
import kinderland.product.model.dto.response.WishlistResponse;
import kinderland.product.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Danh sách yêu thích của user hiện tại (theo email từ header Gateway). Yêu cầu đăng nhập.
 */
@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<BaseResponse<WishlistResponse>> myWishlist(HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK", wishlistService.getMyWishlist(currentEmail())));
    }

    @PostMapping("/items")
    public ResponseEntity<BaseResponse<WishlistResponse>> addItem(@Valid @RequestBody WishlistItemRequest request, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Đã thêm vào yêu thích",
                wishlistService.addItem(currentEmail(), request.getProductId())));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<BaseResponse<WishlistResponse>> removeItem(@PathVariable Long id, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Đã xoá khỏi yêu thích",
                wishlistService.removeItem(currentEmail(), id)));
    }

    private String currentEmail() {
        String email = GatewayAuthContext.getCurrentEmail();
        if (email == null) {
            throw new AppException(ErrorCode.MISSING_TOKEN);
        }
        return email;
    }
}

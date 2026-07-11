package kinderland.order.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kinderland.common.dto.BaseResponse;
import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.common.security.GatewayAuthContext;
import kinderland.order.model.dto.request.AddToCartRequest;
import kinderland.order.model.dto.response.CartResponse;
import kinderland.order.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Giỏ hàng (khớp FE): item theo skuId + storeId.
 *  GET /cart · POST /cart/add · PUT /cart/{cartItemId}?quantity= · DELETE /cart/{cartItemId}
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<BaseResponse<CartResponse>> myCart(HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK", cartService.getMyCart(currentEmail())));
    }

    @PostMapping("/add")
    public ResponseEntity<BaseResponse<CartResponse>> addItem(@Valid @RequestBody AddToCartRequest request, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Đã thêm vào giỏ",
                cartService.addToCart(currentEmail(), request)));
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<BaseResponse<CartResponse>> updateItem(@PathVariable Long cartItemId,
                                                                 @RequestParam int quantity, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Đã cập nhật giỏ hàng",
                cartService.updateItemQuantity(currentEmail(), cartItemId, quantity)));
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<BaseResponse<CartResponse>> removeItem(@PathVariable Long cartItemId, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Đã xoá khỏi giỏ",
                cartService.removeItem(currentEmail(), cartItemId)));
    }

    private String currentEmail() {
        String email = GatewayAuthContext.getCurrentEmail();
        if (email == null) {
            throw new AppException(ErrorCode.MISSING_TOKEN);
        }
        return email;
    }
}

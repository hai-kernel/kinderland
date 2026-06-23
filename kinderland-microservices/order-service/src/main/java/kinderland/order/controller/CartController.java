package kinderland.order.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import kinderland.common.dto.BaseResponse;
import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.common.security.GatewayAuthContext;
import kinderland.order.model.dto.request.AddToCartRequest;
import kinderland.order.model.dto.response.CartResponse;
import kinderland.order.service.CartService;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<BaseResponse<CartResponse>> myCart(HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK", cartService.getMyCart(currentEmail())));
    }

    @PostMapping("/items")
    public ResponseEntity<BaseResponse<CartResponse>> addItem(@Valid @RequestBody AddToCartRequest request, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Đã thêm vào giỏ", cartService.addToCart(currentEmail(), request)));
    }

    @DeleteMapping
    public ResponseEntity<BaseResponse<Void>> clear(HttpServletRequest req) {
        cartService.clearCart(currentEmail());
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Đã xoá giỏ hàng", null));
    }

    private String currentEmail() {
        String email = GatewayAuthContext.getCurrentEmail();
        if (email == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        return email;
    }
}

package kinderland.order.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import kinderland.common.dto.BaseResponse;
import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.common.security.GatewayAuthContext;
import kinderland.order.model.dto.request.CreateOrderRequest;
import kinderland.order.model.dto.response.OrderResponse;
import kinderland.order.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<BaseResponse<OrderResponse>> create(@Valid @RequestBody CreateOrderRequest request, HttpServletRequest req) {
        OrderResponse response = orderService.createOrder(currentEmail(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.ok(201, req.getRequestURI(), "Đặt hàng thành công", response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<OrderResponse>>> myOrders(HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK", orderService.getMyOrders(currentEmail())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<OrderResponse>> getById(@PathVariable Long id, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK", orderService.getById(id, currentEmail())));
    }

    /** Email do Gateway gắn vào header sau khi verify JWT. Bắt buộc đăng nhập cho thao tác đơn hàng. */
    private String currentEmail() {
        String email = GatewayAuthContext.getCurrentEmail();
        if (email == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        return email;
    }
}

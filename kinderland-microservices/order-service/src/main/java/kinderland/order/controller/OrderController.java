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
import kinderland.order.model.dto.request.CheckoutRequest;
import kinderland.order.model.dto.request.CreateOrderRequest;
import kinderland.order.model.dto.request.UpdateOrderStatusRequest;
import kinderland.order.model.dto.response.CheckoutResponse;
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

    /** Chủ đơn checkout: chọn phương thức thanh toán; trả paymentUrl (VNPAY) hoặc xác nhận (COD). */
    @PostMapping("/{id}/checkout")
    public ResponseEntity<BaseResponse<CheckoutResponse>> checkout(@PathVariable Long id,
                                                                   @Valid @RequestBody CheckoutRequest request,
                                                                   HttpServletRequest req) {
        CheckoutResponse res = orderService.checkout(id, currentEmail(), request.getMethod(), req.getRemoteAddr());
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Khởi tạo thanh toán thành công", res));
    }

    /** Chủ đơn huỷ đơn (khi còn PENDING/PAID). */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<BaseResponse<OrderResponse>> cancel(@PathVariable Long id, HttpServletRequest req) {
        OrderResponse res = orderService.cancelOrder(id, currentEmail());
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Đã huỷ đơn hàng", res));
    }

    /** ADMIN cập nhật trạng thái đơn (giao hàng/hoàn tất...). */
    @PatchMapping("/{id}")
    public ResponseEntity<BaseResponse<OrderResponse>> updateStatus(@PathVariable Long id,
                                                                    @Valid @RequestBody UpdateOrderStatusRequest request,
                                                                    HttpServletRequest req) {
        OrderResponse res = orderService.updateStatus(id, request.getStatus(), currentRole());
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Cập nhật trạng thái đơn hàng", res));
    }

    /** Email injected by the API Gateway after JWT verification. Login is required for all order operations. */
    private String currentEmail() {
        String email = GatewayAuthContext.getCurrentEmail();
        if (email == null) {
            throw new AppException(ErrorCode.MISSING_TOKEN);
        }
        return email;
    }

    /** Role do Gateway gắn (vd "ROLE_ADMIN"); dùng để phân quyền thao tác admin. */
    private String currentRole() {
        return GatewayAuthContext.getCurrentRole();
    }
}

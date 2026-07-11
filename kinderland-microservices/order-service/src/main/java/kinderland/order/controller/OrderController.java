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
import kinderland.order.model.dto.request.OrderLineRequest;
import kinderland.order.model.dto.request.UpdateOrderStatusRequest;
import kinderland.order.model.dto.response.CheckoutResponse;
import kinderland.order.model.dto.response.OrderResponse;
import kinderland.order.service.OrderService;

import java.util.List;

/**
 * Đơn hàng (khớp FE): tạo đơn theo store + địa chỉ, item theo skuId.
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** Tạo đơn: POST /orders/create?addressId=&storeId= , body = [{skuId, quantity}]. */
    @PostMapping("/create")
    public ResponseEntity<BaseResponse<OrderResponse>> create(@RequestParam Long addressId,
                                                              @RequestParam Long storeId,
                                                              @Valid @RequestBody List<OrderLineRequest> items,
                                                              HttpServletRequest req) {
        OrderResponse response = orderService.createOrder(currentEmail(), addressId, storeId, items);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.ok(201, req.getRequestURI(), "Đặt hàng thành công", response));
    }

    /** Đơn của tôi (FE gọi /my-orders). */
    @GetMapping({"", "/my-orders"})
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
        CheckoutResponse res = orderService.checkout(id, currentEmail(), request.getPaymentMethod(),
                request.getPointsToUse(), req.getRemoteAddr());
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Khởi tạo thanh toán thành công", res));
    }

    /** Chủ đơn huỷ đơn (khi còn PENDING). */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<BaseResponse<OrderResponse>> cancel(@PathVariable Long id, HttpServletRequest req) {
        OrderResponse res = orderService.cancelOrder(id, currentEmail());
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Đã huỷ đơn hàng", res));
    }

    /** ADMIN cập nhật trạng thái đơn (FE gửi { orderStatus }). */
    @PatchMapping("/{id}")
    public ResponseEntity<BaseResponse<OrderResponse>> updateStatus(@PathVariable Long id,
                                                                    @Valid @RequestBody UpdateOrderStatusRequest request,
                                                                    HttpServletRequest req) {
        OrderResponse res = orderService.updateStatus(id, request.getOrderStatus(), currentRole());
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Cập nhật trạng thái đơn hàng", res));
    }

    private String currentEmail() {
        String email = GatewayAuthContext.getCurrentEmail();
        if (email == null) {
            throw new AppException(ErrorCode.MISSING_TOKEN);
        }
        return email;
    }

    private String currentRole() {
        return GatewayAuthContext.getCurrentRole();
    }
}

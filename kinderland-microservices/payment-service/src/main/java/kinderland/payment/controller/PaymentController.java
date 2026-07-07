package kinderland.payment.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kinderland.common.dto.BaseResponse;
import kinderland.payment.model.dto.response.PaymentResponse;
import kinderland.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

/**
 * API thanh toán công khai (route qua Gateway tại /api/v1/payment/**).
 *  - /vnpay-return, /verify-vnpay là PUBLIC (VNPay/khách gọi không có JWT — xem PUBLIC_PATHS ở Gateway).
 *  - /order/{orderId} yêu cầu đăng nhập (Gateway chặn nếu thiếu token).
 */
@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /** VNPay redirect trình duyệt về đây sau khi thanh toán -> chuyển tiếp tới trang kết quả frontend. */
    @GetMapping("/vnpay-return")
    public void vnpayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String redirectUrl = paymentService.handleVnpayReturn(request);
        response.sendRedirect(redirectUrl);
    }

    /** Frontend gọi để xác minh kết quả (trả JSON status: success/failed/cancelled/invalid). */
    @PostMapping("/verify-vnpay")
    public ResponseEntity<Map<String, String>> verifyVnpay(@RequestBody Map<String, String> vnpayParams) {
        return ResponseEntity.ok(Map.of("status", paymentService.verifyVnpay(vnpayParams)));
    }

    /**
     * VNPay IPN (server-to-server). VNPay gọi trực tiếp endpoint này để chốt kết quả — đáng tin hơn
     * return-url (không phụ thuộc trình duyệt khách). Trả JSON {RspCode, Message} theo chuẩn VNPay.
     */
    @GetMapping("/vnpay-ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(HttpServletRequest request) {
        return ResponseEntity.ok(paymentService.handleVnpayIpn(kinderland.payment.util.VNPayUtils.getVNPayResponseParams(request)));
    }

    /** Xem trạng thái thanh toán của một đơn (yêu cầu đăng nhập). */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<BaseResponse<PaymentResponse>> getByOrder(@PathVariable Long orderId, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(HttpStatus.OK.value(), req.getRequestURI(), "OK",
                paymentService.getByOrderId(orderId)));
    }
}

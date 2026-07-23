package kinderland.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kinderland.common.dto.BaseResponse;
import kinderland.payment.model.dto.response.PaymentResponse;
import kinderland.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    private final ObjectMapper objectMapper;

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
     *
     * GHI THẲNG vào HttpServletResponse thay vì trả ResponseEntity<Map>.
     *
     * Bản cũ để Spring tự chọn message converter, nên phản hồi phụ thuộc header Accept của
     * bên gọi. VNPay gửi Accept KHÔNG phải JSON -> Spring trả 406 Not Acceptable và
     * handler KHÔNG BAO GIỜ CHẠY: thanh toán thành công vẫn nằm PENDING, sau 15 phút bị
     * OrderExpiryScheduler huỷ. Kiểm chứng:
     *     curl -H "Accept: text/html" .../vnpay-ipn   -> 406
     *     curl -H "Accept: *&#47;*"      .../vnpay-ipn   -> 200
     * Ghi trực tiếp thì bỏ qua hoàn toàn content negotiation, Accept gì cũng nhận.
     */
    @GetMapping("/vnpay-ipn")
    public void vnpayIpn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> body = paymentService.handleVnpayIpn(
                kinderland.payment.util.VNPayUtils.getVNPayResponseParams(request));

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    /** Xem trạng thái thanh toán của một đơn (yêu cầu đăng nhập). */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<BaseResponse<PaymentResponse>> getByOrder(@PathVariable Long orderId, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(HttpStatus.OK.value(), req.getRequestURI(), "OK",
                paymentService.getByOrderId(orderId)));
    }
}

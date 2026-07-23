package kinderland.payment.service;

import jakarta.servlet.http.HttpServletRequest;
import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.payment.event.PaymentCompletedEvent;
import kinderland.payment.event.PaymentEventPublisher;
import kinderland.payment.mapper.PaymentMapper;
import kinderland.payment.model.dto.request.InitiatePaymentRequest;
import kinderland.payment.model.dto.response.PaymentInitResponse;
import kinderland.payment.model.dto.response.PaymentResponse;
import kinderland.payment.model.entity.Payment;
import kinderland.payment.model.entity.PaymentMethod;
import kinderland.payment.model.entity.PaymentStatus;
import kinderland.payment.repository.PaymentRepository;
import kinderland.payment.util.VNPayUtils;
import kinderland.payment.vnpay.VNPayGateway;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PaymentService {

    PaymentRepository paymentRepository;
    VNPayGateway vnPayGateway;
    PaymentEventPublisher eventPublisher;
    PaymentMapper paymentMapper;
    kinderland.payment.config.VnPayProperties vnPayProperties;

    @Value("${vnpay.secret-key}")
    @lombok.experimental.NonFinal
    String vnpSecretKey;

    @Value("${vnpay.frontend-url}")
    @lombok.experimental.NonFinal
    String frontendUrl;

    // =====================================================================
    // 1) KHỞI TẠO THANH TOÁN (order-service gọi qua Feign lúc checkout)
    // =====================================================================
    @Transactional
    public PaymentInitResponse initiate(InitiatePaymentRequest req) {
        Payment payment = paymentRepository.findByOrderId(req.getOrderId()).orElse(null);
        if (payment != null && payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_PAID);
        }
        if (payment == null) {
            payment = Payment.builder()
                    .orderId(req.getOrderId())
                    .accountEmail(req.getAccountEmail())
                    .createdAt(LocalDateTime.now())
                    .build();
        }
        payment.setAmount(req.getAmount());
        payment.setMethod(req.getMethod());
        payment.setStatus(PaymentStatus.PENDING);

        if (req.getMethod() == PaymentMethod.COD) {
            // COD: ghi nhận thành công ngay, đơn sẽ chuyển PAID qua event (thu tiền khi giao).
            markSuccess(payment, null);
            return PaymentInitResponse.builder()
                    .orderId(payment.getOrderId())
                    .method(payment.getMethod().name())
                    .status(payment.getStatus().name())
                    .paymentUrl(null)
                    .build();
        }

        // ĐÃ XOÁ nhánh "SIMULATED": trước đây khi vnpay.secret-key trống thì tự đánh SUCCESS,
        // nghĩa là mọi đơn được coi như đã thanh toán mà không ai trả tiền. Giờ cấu hình thiếu
        // sẽ khiến service không khởi động được (VnPayProperties @Validated @NotBlank).

        // VNPAY: giữ PENDING, trả URL để client redirect sang cổng thanh toán.
        String txnRef = payment.getOrderId() + "_" + System.currentTimeMillis();
        payment.setTxnRef(txnRef);
        paymentRepository.save(payment);
        String ip = (req.getIpAddress() == null || req.getIpAddress().isBlank()) ? "127.0.0.1" : req.getIpAddress();
        String url = vnPayGateway.createPaymentUrl(txnRef, payment.getAmount(), ip);

        return PaymentInitResponse.builder()
                .orderId(payment.getOrderId())
                .method(payment.getMethod().name())
                .status(payment.getStatus().name())
                .paymentUrl(url)
                .build();
    }

    // =====================================================================
    // 2) VNPay REDIRECT VỀ (browser) -> trả URL trang kết quả của frontend
    // =====================================================================
    @Transactional(readOnly = true)
    public String handleVnpayReturn(HttpServletRequest request) {
        Map<String, String> params = VNPayUtils.getVNPayResponseParams(request);
        String outcome = readVnpayOutcome(params);

        // Kèm orderId (vnp_TxnRef) để trang kết quả hiển thị được mã đơn và tra cứu tiếp.
        // Trước đây chỉ trả ?status= nên mọi tham số VNPay bị mất sau redirect.
        String orderId = params.getOrDefault("vnp_TxnRef", "");

        StringBuilder url = new StringBuilder(frontendUrl)
                .append("/payment-result?status=")
                .append(URLEncoder.encode(outcome, StandardCharsets.UTF_8));
        if (!orderId.isBlank()) {
            url.append("&orderId=").append(URLEncoder.encode(orderId, StandardCharsets.UTF_8));
        }
        return url.toString();
    }

    // =====================================================================
    // 3) FRONTEND gọi verify (sau khi VNPay redirect) -> trả status string
    // =====================================================================
    @Transactional(readOnly = true)
    public String verifyVnpay(Map<String, String> params) {
        return readVnpayOutcome(params);
    }

    /**
     * CHỈ ĐỌC. Return URL/verify không phải nguồn sự thật: trình duyệt có thể bị chỉnh sửa,
     * bỏ qua, hoặc gọi lại nhiều lần. Chỉ IPN (server-to-server, có chữ ký) được cập nhật DB.
     *
     * Trả trạng thái ĐANG LƯU trong DB (do IPN ghi), không phải trạng thái suy ra từ query.
     * Nếu IPN chưa kịp tới, trả "pending" để frontend hiển thị "đang xác nhận" rồi poll tiếp.
     */
    private String readVnpayOutcome(Map<String, String> params) {
        if (!VNPayUtils.verifySignature(params, vnpSecretKey)) {
            return "invalid";
        }
        Long orderId = parseOrderId(params.get("vnp_TxnRef"));
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        return switch (payment.getStatus()) {
            case SUCCESS -> "success";
            case FAILED -> "failed";
            default -> "pending";
        };
    }

    // =====================================================================
    // 4) VNPay IPN (server-to-server) — VNPay gọi để CHỐT kết quả đáng tin cậy
    //    (không phụ thuộc việc trình duyệt khách có quay về return-url hay không).
    //    Trả JSON theo đúng format VNPay quy định: {RspCode, Message}.
    // =====================================================================
    @Transactional
    public Map<String, String> handleVnpayIpn(Map<String, String> params) {
        try {
            return processVnpayIpn(params);
        } catch (Exception e) {
            // Bao toàn bộ xử lý: VNPay chỉ hiểu {RspCode, Message}. Nếu để exception thoát ra,
            // Spring trả HTML/JSON lỗi mà VNPay không parse được và sẽ retry vô ích.
            // KHÔNG trả stack trace cho VNPay; log lại phía mình để điều tra.
            log.error("Lỗi ngoài dự kiến khi xử lý VNPay IPN. txnRef={}", params.get("vnp_TxnRef"), e);
            return ipnResponse("99", "Unknown error");
        }
    }

    private Map<String, String> processVnpayIpn(Map<String, String> params) {
        if (!VNPayUtils.verifySignature(params, vnpSecretKey)) {
            return ipnResponse("97", "Invalid Checksum");
        }

        // Callback phải thuộc đúng merchant của mình. Chữ ký đúng nhưng TmnCode lạ
        // nghĩa là cấu hình sai hoặc callback không dành cho hệ thống này -> KHÔNG cập nhật gì.
        String callbackTmnCode = params.get("vnp_TmnCode");
        if (callbackTmnCode == null || !callbackTmnCode.equals(vnPayProperties.getTmnCode())) {
            log.warn("IPN bị từ chối: vnp_TmnCode không khớp merchant. txnRef={}", params.get("vnp_TxnRef"));
            // VNPay không định nghĩa mã riêng cho TmnCode sai. Dùng 01 (Order not Found)
            // vì với merchant này giao dịch đó thực sự không tồn tại — giữ đúng contract VNPay.
            return ipnResponse("01", "Order not found");
        }

        // CHỈ bắt lỗi PARSE txnRef -> đó mới thực sự là "không tìm thấy đơn" (01).
        // KHÔNG bắt lỗi hạ tầng ở đây: catch(Exception) rộng trước đây biến lỗi DB thành 01,
        // khiến VNPay tưởng đơn không tồn tại và NGỪNG RETRY -> mất giao dịch.
        // Lỗi DB phải thoát ra outer catch để trả 99, VNPay sẽ retry lại sau.
        Long orderId;
        try {
            orderId = parseOrderId(params.get("vnp_TxnRef"));
        } catch (AppException | NumberFormatException e) {
            return ipnResponse("01", "Order not found");
        }

        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        if (payment == null) {
            return ipnResponse("01", "Order not found");
        }

        // Kiểm tra số tiền khớp (chống giả mạo): vnp_Amount = amount * 100.
        try {
            long vnpAmount = Long.parseLong(params.get("vnp_Amount"));
            long expected = payment.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
            if (vnpAmount != expected) {
                return ipnResponse("04", "Invalid amount");
            }
        } catch (Exception e) {
            return ipnResponse("04", "Invalid amount");
        }

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return ipnResponse("02", "Order already confirmed");
        }

        // VNPay yêu cầu CẢ HAI field đều "00" mới coi là thanh toán thành công.
        // Chỉ dựa vnp_ResponseCode là thiếu: có trường hợp ResponseCode=00 nhưng
        // TransactionStatus khác 00 (giao dịch chưa hoàn tất/bị nghi ngờ) -> KHÔNG được đánh SUCCESS.
        boolean paid = "00".equals(params.get("vnp_ResponseCode"))
                && "00".equals(params.get("vnp_TransactionStatus"));

        if (paid) {
            markSuccess(payment, params.get("vnp_TransactionNo"));
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }
        return ipnResponse("00", "Confirm Success");
    }

    private Map<String, String> ipnResponse(String code, String message) {
        return Map.of("RspCode", code, "Message", message);
    }

    public PaymentResponse getByOrderId(Long orderId) {
        return paymentMapper.toResponse(paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND)));
    }

    /** Đánh dấu thành công + bắn event để order-service set đơn PAID. */
    private void markSuccess(Payment payment, String transactionCode) {
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionCode(transactionCode);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        eventPublisher.publishPaymentCompleted(PaymentCompletedEvent.builder()
                .orderId(payment.getOrderId())
                .accountEmail(payment.getAccountEmail())
                .transactionCode(transactionCode)
                .build());
    }

    private Long parseOrderId(String txnRef) {
        if (txnRef == null) {
            throw new AppException(ErrorCode.PAYMENT_NOT_FOUND);
        }
        String raw = txnRef.contains("_") ? txnRef.split("_")[0] : txnRef;
        return Long.parseLong(raw);
    }
}

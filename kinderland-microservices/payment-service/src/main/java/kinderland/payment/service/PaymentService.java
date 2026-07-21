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

        // VNPAY nhưng CHƯA cấu hình cổng (dev/demo: secret-key trống) → mô phỏng thành công ngay,
        // tránh crash HMAC "Empty key" + để luồng "card" trên FE chạy trọn vẹn (redirect về trang kết quả).
        if (vnpSecretKey == null || vnpSecretKey.isBlank()) {
            log.warn("VNPay chưa cấu hình (vnpay.secret-key trống) → MÔ PHỎNG thanh toán SUCCESS cho orderId={}. "
                    + "Cấu hình VNPAY_TMN_CODE/VNPAY_SECRET_KEY để dùng cổng thật.", payment.getOrderId());
            markSuccess(payment, "SIMULATED");
            return PaymentInitResponse.builder()
                    .orderId(payment.getOrderId())
                    .method(payment.getMethod().name())
                    .status(payment.getStatus().name())   // SUCCESS
                    // Cùng trang kết quả với luồng VNPay thật (handleVnpayReturn) để FE hiển thị nhất quán.
                    .paymentUrl(frontendUrl + "/payment-result?status=success")
                    .build();
        }

        // VNPAY (đã cấu hình): giữ PENDING, trả URL để client redirect sang cổng thanh toán.
        paymentRepository.save(payment);
        String txnRef = payment.getOrderId() + "_" + System.currentTimeMillis();
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
    @Transactional
    public String handleVnpayReturn(HttpServletRequest request) {
        Map<String, String> params = VNPayUtils.getVNPayResponseParams(request);
        String outcome = applyVnpayResult(params);
        return frontendUrl + "/payment-result?status=" + outcome;
    }

    // =====================================================================
    // 3) FRONTEND gọi verify (sau khi VNPay redirect) -> trả status string
    // =====================================================================
    @Transactional
    public String verifyVnpay(Map<String, String> params) {
        return applyVnpayResult(params);
    }

    /** Lõi xử lý kết quả VNPay dùng chung cho cả return & verify. */
    private String applyVnpayResult(Map<String, String> params) {
        if (!VNPayUtils.verifySignature(params, vnpSecretKey)) {
            return "invalid";
        }
        Long orderId = parseOrderId(params.get("vnp_TxnRef"));
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return "success"; // đã xử lý trước đó (idempotent)
        }

        String responseCode = params.get("vnp_ResponseCode");
        if ("00".equals(responseCode)) {
            markSuccess(payment, params.get("vnp_TransactionNo"));
            return "success";
        } else if ("24".equals(responseCode)) {
            // Người dùng huỷ trên cổng -> giữ PENDING để có thể thử lại.
            payment.setStatus(PaymentStatus.PENDING);
            paymentRepository.save(payment);
            return "cancelled";
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            return "failed";
        }
    }

    // =====================================================================
    // 4) VNPay IPN (server-to-server) — VNPay gọi để CHỐT kết quả đáng tin cậy
    //    (không phụ thuộc việc trình duyệt khách có quay về return-url hay không).
    //    Trả JSON theo đúng format VNPay quy định: {RspCode, Message}.
    // =====================================================================
    @Transactional
    public Map<String, String> handleVnpayIpn(Map<String, String> params) {
        if (!VNPayUtils.verifySignature(params, vnpSecretKey)) {
            return ipnResponse("97", "Invalid Checksum");
        }

        Payment payment;
        try {
            payment = paymentRepository.findByOrderId(parseOrderId(params.get("vnp_TxnRef"))).orElse(null);
        } catch (Exception e) {
            payment = null;
        }
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

        if ("00".equals(params.get("vnp_ResponseCode"))) {
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

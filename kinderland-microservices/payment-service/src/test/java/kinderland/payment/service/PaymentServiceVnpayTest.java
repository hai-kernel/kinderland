package kinderland.payment.service;

import kinderland.payment.config.VnPayProperties;
import kinderland.payment.event.PaymentCompletedEvent;
import kinderland.payment.event.PaymentEventPublisher;
import kinderland.payment.mapper.PaymentMapper;
import kinderland.payment.model.entity.Payment;
import kinderland.payment.model.entity.PaymentMethod;
import kinderland.payment.model.entity.PaymentStatus;
import kinderland.payment.repository.PaymentRepository;
import kinderland.payment.util.HmacUtil;
import kinderland.payment.vnpay.VNPayGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test cho patch VNPay: IPN là nguồn sự thật duy nhất, Return URL chỉ đọc.
 *
 * Secret dùng trong test là chuỗi cố định vô nghĩa, KHÔNG phải secret thật.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentServiceVnpayTest {

    private static final String SECRET = "TEST_ONLY_SECRET_NOT_REAL_0123456789";
    private static final String TMN_CODE = "TESTTMN01";
    private static final String TXN_REF = "77_1700000000000";
    private static final long ORDER_ID = 77L;
    private static final BigDecimal AMOUNT = new BigDecimal("379000");

    @Mock private PaymentRepository paymentRepository;
    @Mock private VNPayGateway vnPayGateway;
    @Mock private PaymentEventPublisher eventPublisher;
    @Mock private PaymentMapper paymentMapper;

    private PaymentService service;

    @BeforeEach
    void setUp() {
        VnPayProperties props = new VnPayProperties();
        props.setTmnCode(TMN_CODE);
        props.setSecretKey(SECRET);
        props.setUrl("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        props.setReturnUrl("http://localhost:8080/api/v1/payment/vnpay-return");
        props.setFrontendUrl("http://localhost:5173");

        service = new PaymentService(paymentRepository, vnPayGateway, eventPublisher, paymentMapper, props);
        ReflectionTestUtils.setField(service, "vnpSecretKey", SECRET);
        ReflectionTestUtils.setField(service, "frontendUrl", "http://localhost:5173");
    }

    // ================= helpers =================

    /** Dựng bộ params VNPay hợp lệ rồi ký đúng thuật toán mà VNPayUtils dùng để verify. */
    private Map<String, String> signedParams(String responseCode, String transactionStatus, String tmnCode, long amountX100) {
        Map<String, String> p = new HashMap<>();
        p.put("vnp_TmnCode", tmnCode);
        p.put("vnp_TxnRef", TXN_REF);
        p.put("vnp_Amount", String.valueOf(amountX100));
        p.put("vnp_ResponseCode", responseCode);
        p.put("vnp_TransactionStatus", transactionStatus);
        p.put("vnp_TransactionNo", "14000001");
        p.put("vnp_BankCode", "NCB");
        p.put("vnp_SecureHash", sign(p));
        return p;
    }

    private String sign(Map<String, String> fields) {
        TreeMap<String, String> sorted = new TreeMap<>();
        fields.forEach((k, v) -> {
            if (k.startsWith("vnp_") && !k.equals("vnp_SecureHash") && !k.equals("vnp_SecureHashType")) {
                sorted.put(k, v);
            }
        });
        StringBuilder sb = new StringBuilder();
        sorted.forEach((k, v) -> {
            if (v != null && !v.isEmpty()) {
                sb.append(k).append('=').append(URLEncoder.encode(v, StandardCharsets.UTF_8)).append('&');
            }
        });
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return HmacUtil.hmacSHA512(SECRET, sb.toString());
    }

    private Payment pendingPayment() {
        return Payment.builder()
                .id(1L).orderId(ORDER_ID).accountEmail("a@b.c")
                .amount(AMOUNT).status(PaymentStatus.PENDING)
                .method(PaymentMethod.VNPAY).txnRef(TXN_REF)
                .build();
    }

    private long amountX100() {
        return AMOUNT.multiply(BigDecimal.valueOf(100)).longValue();
    }

    // ================= D. IPN result =================

    @Test
    @DisplayName("Case 1: ResponseCode=00 + TransactionStatus=00 -> SUCCESS, publish đúng 1 lần, RspCode 00")
    void ipnSuccess() {
        Payment payment = pendingPayment();
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));

        Map<String, String> res = service.handleVnpayIpn(signedParams("00", "00", TMN_CODE, amountX100()));

        assertThat(res.get("RspCode")).isEqualTo("00");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        verify(eventPublisher, times(1)).publishPaymentCompleted(any(PaymentCompletedEvent.class));
    }

    @Test
    @DisplayName("Case 2: ResponseCode=00 nhưng TransactionStatus=02 -> KHÔNG success, không publish")
    void ipnResponseOkButTransactionStatusNotOk() {
        Payment payment = pendingPayment();
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));

        Map<String, String> res = service.handleVnpayIpn(signedParams("00", "02", TMN_CODE, amountX100()));

        assertThat(res.get("RspCode")).isEqualTo("00"); // đã ghi nhận callback
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        verify(eventPublisher, never()).publishPaymentCompleted(any());
    }

    @Test
    @DisplayName("Case 3: TransactionStatus=00 nhưng ResponseCode=24 -> KHÔNG success, không publish")
    void ipnTransactionStatusOkButResponseCodeNotOk() {
        Payment payment = pendingPayment();
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));

        Map<String, String> res = service.handleVnpayIpn(signedParams("24", "00", TMN_CODE, amountX100()));

        assertThat(res.get("RspCode")).isEqualTo("00");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        verify(eventPublisher, never()).publishPaymentCompleted(any());
    }

    @Test
    @DisplayName("Case 4: TmnCode sai -> RspCode 01, KHÔNG save, KHÔNG publish")
    void ipnWrongTmnCode() {
        Map<String, String> res = service.handleVnpayIpn(signedParams("00", "00", "SOMEONE_ELSE", amountX100()));

        assertThat(res.get("RspCode")).isEqualTo("01");
        verify(paymentRepository, never()).save(any());
        verify(eventPublisher, never()).publishPaymentCompleted(any());
    }

    @Test
    @DisplayName("Case 5: Checksum sai -> RspCode 97, không đụng DB")
    void ipnInvalidChecksum() {
        Map<String, String> p = signedParams("00", "00", TMN_CODE, amountX100());
        p.put("vnp_SecureHash", "deadbeef");

        Map<String, String> res = service.handleVnpayIpn(p);

        assertThat(res.get("RspCode")).isEqualTo("97");
        verify(paymentRepository, never()).save(any());
        verify(eventPublisher, never()).publishPaymentCompleted(any());
    }

    @Test
    @DisplayName("Case 6: TxnRef không tồn tại -> RspCode 01, không publish")
    void ipnPaymentNotFound() {
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

        Map<String, String> res = service.handleVnpayIpn(signedParams("00", "00", TMN_CODE, amountX100()));

        assertThat(res.get("RspCode")).isEqualTo("01");
        verify(eventPublisher, never()).publishPaymentCompleted(any());
    }

    @Test
    @DisplayName("Case 7: Amount không khớp -> RspCode 04, không update, không publish")
    void ipnInvalidAmount() {
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(pendingPayment()));

        Map<String, String> res = service.handleVnpayIpn(signedParams("00", "00", TMN_CODE, 999L));

        assertThat(res.get("RspCode")).isEqualTo("04");
        verify(paymentRepository, never()).save(any());
        verify(eventPublisher, never()).publishPaymentCompleted(any());
    }

    @Test
    @DisplayName("Case 8: Payment đã SUCCESS -> RspCode 02, không save lại, không publish lại")
    void ipnAlreadyConfirmed() {
        Payment paid = pendingPayment();
        paid.setStatus(PaymentStatus.SUCCESS);
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(paid));

        Map<String, String> res = service.handleVnpayIpn(signedParams("00", "00", TMN_CODE, amountX100()));

        assertThat(res.get("RspCode")).isEqualTo("02");
        verify(paymentRepository, never()).save(any());
        verify(eventPublisher, never()).publishPaymentCompleted(any());
    }

    @Test
    @DisplayName("Case 9: Exception bất ngờ -> RspCode 99, không lộ stack trace, không trả 00")
    void ipnUnexpectedException() {
        when(paymentRepository.findByOrderId(ORDER_ID)).thenThrow(new RuntimeException("db down"));

        Map<String, String> res = service.handleVnpayIpn(signedParams("00", "00", TMN_CODE, amountX100()));

        assertThat(res.get("RspCode")).isEqualTo("99");
        assertThat(res.get("Message")).isEqualTo("Unknown error");
        assertThat(res.get("Message")).doesNotContain("db down");
        verify(eventPublisher, never()).publishPaymentCompleted(any());
    }

    // ================= E. Idempotency =================

    @Test
    @DisplayName("Idempotency: gửi lại cùng IPN -> vẫn SUCCESS, event KHÔNG publish lần hai")
    void duplicateIpnDoesNotPublishTwice() {
        Payment payment = pendingPayment();
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(payment));
        Map<String, String> ipn = signedParams("00", "00", TMN_CODE, amountX100());

        Map<String, String> first = service.handleVnpayIpn(ipn);
        Map<String, String> second = service.handleVnpayIpn(ipn); // payment giờ đã SUCCESS

        assertThat(first.get("RspCode")).isEqualTo("00");
        assertThat(second.get("RspCode")).isEqualTo("02");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        verify(eventPublisher, times(1)).publishPaymentCompleted(any());
    }

    // ================= G. Return URL read-only =================

    @Test
    @DisplayName("Return URL: KHÔNG save, KHÔNG publish event; trả trạng thái đang lưu trong DB")
    void returnUrlIsReadOnly() {
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(pendingPayment()));

        String outcome = service.verifyVnpay(signedParams("00", "00", TMN_CODE, amountX100()));

        // IPN chưa chạy -> DB vẫn PENDING -> Return phải nói "pending", KHÔNG tự đánh success
        assertThat(outcome).isEqualTo("pending");
        verify(paymentRepository, never()).save(any());
        verify(eventPublisher, never()).publishPaymentCompleted(any());
    }

    @Test
    @DisplayName("Return URL: checksum sai -> 'invalid', không đụng DB")
    void returnUrlInvalidSignature() {
        Map<String, String> p = signedParams("00", "00", TMN_CODE, amountX100());
        p.put("vnp_SecureHash", "bad");

        assertThat(service.verifyVnpay(p)).isEqualTo("invalid");
        verify(paymentRepository, never()).save(any());
        verify(eventPublisher, never()).publishPaymentCompleted(any());
    }

    // ================= C. Signature =================

    @Test
    @DisplayName("HMAC-SHA512 deterministic + hex thường + đổi 1 ký tự thì verify fail")
    void signatureDeterministicAndSensitive() {
        String a = HmacUtil.hmacSHA512(SECRET, "vnp_Amount=100&vnp_TxnRef=abc");
        String b = HmacUtil.hmacSHA512(SECRET, "vnp_Amount=100&vnp_TxnRef=abc");

        assertThat(a).isEqualTo(b).matches("[0-9a-f]{128}");
        assertThat(HmacUtil.hmacSHA512(SECRET, "vnp_Amount=101&vnp_TxnRef=abc")).isNotEqualTo(a);
    }
}

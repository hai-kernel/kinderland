package kinderland.payment.vnpay;

import kinderland.payment.config.VnPayProperties;
import kinderland.payment.util.HmacUtil;
import kinderland.payment.util.VNPayUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.URLDecoder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

/**
 * Sinh URL redirect sang cổng VNPay. Port thuật toán ký từ monolith (giữ nguyên để tương thích VNPay).
 * Config lấy từ prefix vnpay.* trong application.yml (nạp từ biến môi trường VNPAY_*).
 */
@Component
@Slf4j
@EnableConfigurationProperties(VnPayProperties.class)
public class VNPayGateway {

    /** Đổi giá trị này mỗi lần sửa logic ký để xác minh instance đang chạy đúng build. */
    private static final String BUILD_MARKER = "VNPAY-SIGNATURE-FIX-20260722-01";

    private final VnPayProperties props;

    public VNPayGateway(VnPayProperties props) {
        this.props = props;
    }

    public String createPaymentUrl(String txnRef, BigDecimal amount, String ipAddress) {
        String tmnCode = props.getTmnCode();
        String secretKey = props.getSecretKey();
        String vnpUrl = props.getUrl();
        String returnUrl = props.getReturnUrl();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        // VNPay yêu cầu số tiền * 100 (đơn vị nhỏ nhất).
        params.put("vnp_Amount", String.valueOf(amount.multiply(BigDecimal.valueOf(100)).longValue()));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        // KHÔNG dùng dấu cách hay ':' trong OrderInfo.
        // URLEncoder mã hoá dấu cách thành '+' (không phải '%20'); đây là nguyên nhân
        // hay gặp nhất gây "Sai chữ ký" (code 70) vì phía VNPay có thể chuẩn hoá khác.
        // OrderInfo chỉ để hiển thị nên dùng ký tự an toàn là đủ, và loại bỏ hoàn toàn
        // biến số encode khỏi chuỗi ký.
        params.put("vnp_OrderInfo", "ThanhToanDonHang" + txnRef.replace("_", ""));
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", ipAddress);
        params.put("vnp_CreateDate", now.format(formatter));
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(formatter));

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                continue;
            }
            String encoded = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
            hashData.append(entry.getKey()).append("=").append(encoded).append("&");
            query.append(entry.getKey()).append("=").append(encoded).append("&");
        }
        hashData.deleteCharAt(hashData.length() - 1);

        String secureHash = HmacUtil.hmacSHA512(secretKey, hashData.toString());
        query.append("vnp_SecureHash=").append(secureHash);

        String paymentUrl = vnpUrl + "?" + query;

        // Marker để biết CHẮC CHẮN instance đang chạy có phải build mới không.
        // Không thấy dòng này trong log = đang chạy code cũ, mọi chẩn đoán khác đều vô nghĩa.
        log.info("VNPay builder version={}", BUILD_MARKER);

        if (log.isDebugEnabled()) {
            // hashData KHÔNG chứa secret -> an toàn để log ở DEBUG.
            log.debug("VNPay hashData={}", hashData);
            log.debug("VNPay secureHashLength={} secureHashFormatValid={}",
                    secureHash.length(), secureHash.matches("[0-9a-f]{128}"));
            // URL đã che chữ ký: đủ để đối chiếu với request thật trong DevTools
            // mà không đưa chữ ký giao dịch vào log.
            log.debug("VNPay paymentUrl={}",
                    paymentUrl.replaceAll("(vnp_SecureHash=)[0-9a-fA-F]+", "$1***"));
        }

        // Fail-fast: parse NGƯỢC chính URL vừa tạo rồi verify lại.
        // Bắt được lỗi encode/decode round-trip TRƯỚC khi đẩy URL hỏng sang VNPay,
        // thay vì để VNPay trả code=70 rồi mới biết.
        verifyGeneratedPaymentUrl(paymentUrl, secretKey);

        return paymentUrl;
    }

    /**
     * Parse lại query string của URL vừa sinh và verify chữ ký bằng đúng code dùng cho
     * IPN/Return. Nếu fail nghĩa là bên ký và bên verify bất đồng về quy tắc encode.
     */
    private void verifyGeneratedPaymentUrl(String paymentUrl, String secretKey) {
        int queryStart = paymentUrl.indexOf('?');
        if (queryStart < 0) {
            throw new IllegalStateException("Generated VNPay URL has no query string");
        }

        String rawQuery = paymentUrl.substring(queryStart + 1);
        Map<String, String> parsed = new TreeMap<>();
        for (String part : rawQuery.split("&")) {
            String[] kv = part.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            parsed.put(key, value);
        }

        if (!VNPayUtils.verifySignature(parsed, secretKey)) {
            throw new IllegalStateException("Generated VNPay URL failed parsed-query verification");
        }
    }
}

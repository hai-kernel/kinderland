package kinderland.payment.vnpay;

import kinderland.payment.util.HmacUtil;
import org.springframework.beans.factory.annotation.Value;
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
public class VNPayGateway {

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.secret-key}")
    private String secretKey;

    @Value("${vnpay.url}")
    private String vnpUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    public String createPaymentUrl(String txnRef, BigDecimal amount, String ipAddress) {
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
        params.put("vnp_OrderInfo", "Thanh toan don hang: " + txnRef);
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

        return vnpUrl + "?" + query;
    }
}

package kinderland.payment.util;

import jakarta.servlet.http.HttpServletRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/** Tiện ích verify chữ ký & đọc tham số callback VNPay. Port từ monolith (đã bỏ log debug). */
public final class VNPayUtils {

    private VNPayUtils() {
    }

    /** Verify chữ ký vnp_SecureHash trong params VNPay trả về. */
    public static boolean verifySignature(Map<String, String> fields, String secretKey) {
        SortedMap<String, String> sorted = new TreeMap<>();
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            if (entry.getKey().startsWith("vnp_")) {
                sorted.put(entry.getKey(), entry.getValue());
            }
        }

        String receivedHash = sorted.remove("vnp_SecureHash");
        sorted.remove("vnp_SecureHashType");

        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                hashData.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                        .append("&");
            }
        }
        if (hashData.length() > 0) {
            hashData.deleteCharAt(hashData.length() - 1);
        }

        String calculatedHash = HmacUtil.hmacSHA512(secretKey, hashData.toString());
        return constantTimeHexEquals(calculatedHash, receivedHash);
    }

    /**
     * So sánh chữ ký bằng constant-time để không rò rỉ thông tin qua thời gian phản hồi.
     *
     * String.equals/equalsIgnoreCase thoát sớm ở byte đầu tiên khác nhau, cho phép kẻ tấn công
     * dò dần từng ký tự chữ ký qua đo thời gian. MessageDigest.isEqual so sánh toàn bộ độ dài.
     *
     * VNPay có thể trả hex hoa hoặc thường -> chuẩn hoá về chữ thường trước khi so sánh
     * (giữ đúng hành vi cũ của equalsIgnoreCase, chỉ khác ở chỗ không thoát sớm).
     */
    static boolean constantTimeHexEquals(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        byte[] a = expected.toLowerCase(java.util.Locale.ROOT).getBytes(StandardCharsets.US_ASCII);
        byte[] b = actual.toLowerCase(java.util.Locale.ROOT).getBytes(StandardCharsets.US_ASCII);
        return java.security.MessageDigest.isEqual(a, b);
    }

    /** Gom toàn bộ query param của request callback thành Map. */
    public static Map<String, String> getVNPayResponseParams(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            String value = request.getParameter(name);
            if (value != null && !value.isEmpty()) {
                fields.put(name, value);
            }
        }
        return fields;
    }
}

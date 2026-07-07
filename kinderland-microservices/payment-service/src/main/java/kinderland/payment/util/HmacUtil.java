package kinderland.payment.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/** HMAC-SHA512 để ký/verify dữ liệu VNPay. Port nguyên vẹn từ monolith. */
public final class HmacUtil {

    private HmacUtil() {
    }

    public static String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                return null;
            }
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate HMAC SHA-512 hash", ex);
        }
    }
}

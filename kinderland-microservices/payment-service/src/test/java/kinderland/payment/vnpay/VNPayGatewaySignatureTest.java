package kinderland.payment.vnpay;

import kinderland.payment.config.VnPayProperties;
import kinderland.payment.util.VNPayUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Chẩn đoán lỗi VNPay code=70 (Sai chữ ký).
 *
 * Mục tiêu: in ra CHUỖI ĐƯỢC KÝ và kiểm tra tính tự nhất quán giữa
 * VNPayGateway (bên ký) và VNPayUtils (bên verify).
 *
 * Secret dùng ở đây là chuỗi test 32 ký tự, KHÔNG phải secret thật.
 */
class VNPayGatewaySignatureTest {

    /** 32 ký tự — đúng độ dài secret sandbox, nhưng là giá trị giả. */
    private static final String TEST_SECRET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ012345";
    private static final String TEST_TMN = "CFTS0LCM";

    private VNPayGateway gateway() {
        VnPayProperties p = new VnPayProperties();
        p.setTmnCode(TEST_TMN);
        p.setSecretKey(TEST_SECRET);
        p.setUrl("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        p.setReturnUrl("http://localhost:8080/api/v1/payment/vnpay-return");
        p.setFrontendUrl("http://localhost:5173");
        return new VNPayGateway(p);
    }

    @Test
    @DisplayName("In URL sinh ra + kiểm tra tự nhất quán giữa bên ký và bên verify")
    void inspectGeneratedUrl() {
        String url = gateway().createPaymentUrl("77_1700000000000", new BigDecimal("379000"), "127.0.0.1");

        String query = URI.create(url).getRawQuery();
        System.out.println("=== RAW QUERY ===");
        System.out.println(query);

        // Tách params y như servlet container sẽ làm khi VNPay callback về
        Map<String, String> decoded = new HashMap<>();
        for (String pair : query.split("&")) {
            int i = pair.indexOf('=');
            decoded.put(pair.substring(0, i), URLDecoder.decode(pair.substring(i + 1), StandardCharsets.UTF_8));
        }

        System.out.println("=== DECODED PARAMS ===");
        decoded.forEach((k, v) -> {
            if (!k.equals("vnp_SecureHash")) {
                System.out.println("  " + k + " = " + v);
            }
        });

        String hash = decoded.get("vnp_SecureHash");
        System.out.println("=== SECURE HASH ===");
        System.out.println("  length=" + hash.length() + " lowercaseHex=" + hash.matches("[0-9a-f]{128}"));

        // Kiểm tra bắt buộc
        assertThat(url).startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?");
        assertThat(hash).matches("[0-9a-f]{128}");
        assertThat(decoded.get("vnp_Amount")).isEqualTo("37900000"); // 379000 * 100
        assertThat(decoded.get("vnp_Version")).isEqualTo("2.1.0");
        assertThat(decoded.get("vnp_TmnCode")).isEqualTo(TEST_TMN);

        // TỰ NHẤT QUÁN: bên verify (dùng cho IPN/Return) phải chấp nhận chữ ký
        // do chính bên ký tạo ra. Nếu FAIL -> hai bên dùng quy tắc encode khác nhau.
        boolean selfConsistent = VNPayUtils.verifySignature(decoded, TEST_SECRET);
        System.out.println("=== SELF-CONSISTENT (sign vs verify) = " + selfConsistent + " ===");
        assertThat(selfConsistent)
                .as("VNPayGateway ký và VNPayUtils verify phải dùng CÙNG quy tắc encode")
                .isTrue();
    }
}

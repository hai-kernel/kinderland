package kinderland.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/**
 * Cấu hình VNPay.
 *
 * GIỮ NGUYÊN prefix "vnpay" và tên property đang dùng trong config repo
 * (tmn-code, secret-key, url, return-url, frontend-url) — không đặt tên mới.
 *
 * @Validated + @NotBlank làm payment-service FAIL-FAST lúc khởi động khi thiếu cấu hình,
 * thay cho hành vi cũ nguy hiểm: secret rỗng thì tự đánh SUCCESS mọi giao dịch.
 */
@Validated
@ConfigurationProperties(prefix = "vnpay")
public class VnPayProperties {

    @NotBlank(message = "must not be blank - set environment variable VNPAY_TMN_CODE")
    private String tmnCode;

    /** Bí mật ký HMAC-SHA512. KHÔNG log, KHÔNG expose qua actuator. */
    @NotBlank(message = "must not be blank - set environment variable VNPAY_SECRET_KEY")
    private String secretKey;

    @NotBlank(message = "must not be blank - VNPay payment gateway URL")
    private String url;

    @NotBlank(message = "must not be blank - VNPay return URL")
    private String returnUrl;

    @NotBlank(message = "must not be blank - frontend base URL for redirect")
    private String frontendUrl;

    public String getTmnCode() {
        return tmnCode;
    }

    /**
     * TRIM bắt buộc: credentials thường được copy từ email VNPay vào biến môi trường và
     * dính khoảng trắng hoặc newline ở cuối. Chỉ 1 ký tự thừa trong TmnCode/SecretKey là
     * chữ ký HMAC khác hoàn toàn -> VNPay trả "Sai chữ ký" (code 70).
     */
    public void setTmnCode(String tmnCode) {
        this.tmnCode = tmnCode == null ? null : tmnCode.trim();
    }

    public String getSecretKey() {
        return secretKey;
    }

    /** TRIM bắt buộc — xem giải thích ở setTmnCode. */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey == null ? null : secretKey.trim();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url == null ? null : url.trim();
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl == null ? null : returnUrl.trim();
    }

    public String getFrontendUrl() {
        return frontendUrl;
    }

    public void setFrontendUrl(String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    /** Không bao giờ in secretKey ra log. */
    @Override
    public String toString() {
        return "VnPayProperties{tmnCode=" + tmnCode + ", url=" + url
                + ", returnUrl=" + returnUrl + ", frontendUrl=" + frontendUrl
                + ", secretKey=***}";
    }
}

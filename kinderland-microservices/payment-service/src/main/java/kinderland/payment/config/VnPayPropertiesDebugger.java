package kinderland.payment.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Log cấu hình VNPay lúc khởi động để chẩn đoán, KHÔNG lộ bí mật.
 *
 * Phiên bản trước in thẳng secret ra INFO ("VNPAY_SR = {}"), khiến hash secret nằm trong
 * log file, log aggregator và console. Secret KHÔNG BAO GIỜ được log dù ở mức nào.
 *
 * Ở đây chỉ log đủ để chẩn đoán: có cấu hình hay chưa, độ dài, 4 ký tự cuối của TmnCode.
 */
@Component
public class VnPayPropertiesDebugger {

    private static final Logger log =
            LoggerFactory.getLogger(VnPayPropertiesDebugger.class);

    private final VnPayProperties properties;

    public VnPayPropertiesDebugger(VnPayProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void debug() {
        String tmnCode = properties.getTmnCode();
        String secretKey = properties.getSecretKey();

        log.info("VNPay config -> tmnCodePresent={} tmnCodeLast4={} tmnCodeLength={}",
                isPresent(tmnCode), last4(tmnCode), length(tmnCode));

        // CHỈ log CÓ/KHÔNG và độ dài. Tuyệt đối không log giá trị secret.
        log.info("VNPay config -> secretKeyPresent={} secretKeyLength={}",
                isPresent(secretKey), length(secretKey));

        log.info("VNPay config -> paymentUrl={} returnUrl={} frontendUrl={}",
                properties.getUrl(), properties.getReturnUrl(), properties.getFrontendUrl());

        // Khoảng trắng thừa là nguyên nhân phổ biến gây SAI CHỮ KÝ khi copy/paste
        // credentials từ email VNPay vào biến môi trường.
        warnIfPadded("vnpay.tmn-code", tmnCode);
        warnIfPadded("vnpay.secret-key", secretKey);
    }

    private void warnIfPadded(String name, String value) {
        if (value != null && !value.equals(value.trim())) {
            log.warn("{} có khoảng trắng ở đầu/cuối -> gây SAI CHỮ KÝ. Xoá khoảng trắng trong biến môi trường.", name);
        }
    }

    private boolean isPresent(String v) {
        return v != null && !v.isBlank();
    }

    private int length(String v) {
        return v == null ? 0 : v.length();
    }

    private String last4(String v) {
        if (v == null || v.length() < 4) {
            return "****";
        }
        return "****" + v.substring(v.length() - 4);
    }
}

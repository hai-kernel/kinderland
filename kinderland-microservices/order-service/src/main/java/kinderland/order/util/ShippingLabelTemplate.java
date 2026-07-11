package kinderland.order.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Load + render template HTML từ classpath bằng thay thế {{placeholder}} đơn giản.
 * Dùng cho nhãn vận chuyển trả hàng.
 */
@Component
public class ShippingLabelTemplate {

    private static final String TEMPLATE_PATH = "templates/return-shipping-label.html";

    public String render(Map<String, String> values) {
        String template = loadTemplate();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return template;
    }

    private String loadTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);
            try (InputStream is = resource.getInputStream()) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load shipping label template", e);
        }
    }
}

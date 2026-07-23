package kinderland.auth.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kinderland.auth.config.AppProperties;

/**
 * Endpoint chỉ để KIỂM CHỨNG auto-refresh cấu hình (không thuộc nghiệp vụ).
 *
 * GET /api/config-test  ->  {"message": "...", "pid": 12345}
 *
 * pid dùng để chứng minh service KHÔNG restart khi giá trị message thay đổi.
 * Controller KHÔNG cần @RefreshScope: nó đọc qua AppProperties (đã @RefreshScope),
 * nên luôn thấy giá trị mới nhất sau sự kiện refresh.
 */
@RestController
@RequestMapping("/api")
public class ConfigTestController {

    private final AppProperties appProperties;

    public ConfigTestController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @GetMapping("/config-test")
    public Map<String, Object> configTest() {
        return Map.of(
                "message", String.valueOf(appProperties.getMessage()),
                "pid", ProcessHandle.current().pid());
    }
}

package kinderland.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Thuộc tính cấu hình dùng để KIỂM CHỨNG cơ chế refresh runtime qua Spring Cloud Bus.
 *
 * @RefreshScope: bean được tạo lại khi nhận RefreshRemoteApplicationEvent,
 * nhờ đó giá trị mới từ Git/Config Server có hiệu lực mà KHÔNG cần restart service.
 *
 * Không dùng @Value cho mục đích này: @Value được resolve một lần lúc tạo bean
 * và sẽ không cập nhật nếu bean không nằm trong refresh scope.
 */
@Component
@RefreshScope
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

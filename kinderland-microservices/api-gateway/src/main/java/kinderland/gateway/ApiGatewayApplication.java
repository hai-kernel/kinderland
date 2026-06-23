package kinderland.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway - cổng vào duy nhất của hệ thống (port 8080).
 *
 * Nhiệm vụ:
 *  - Định tuyến request tới các service qua Eureka (lb://AUTH-SERVICE, ...).
 *  - Validate JWT TẬP TRUNG tại đây (xem {@code AuthenticationGlobalFilter}),
 *    thay cho việc mỗi service tự validate như monolith cũ.
 *  - Sau khi validate, gắn danh tính người dùng vào header (X-Auth-Email, X-Auth-Role)
 *    để các service downstream tin dùng mà không cần parse lại token.
 *
 * Không dùng @EnableDiscoveryClient vì Spring Boot tự bật khi có eureka-client trên classpath.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}

package kinderland.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Config Server tập trung cho hệ thống Kinderland.
 *
 * Dùng native backend (đọc file YAML trong thư mục config-repository) để phục vụ
 * cấu hình cho tất cả service qua HTTP:
 *   GET http://localhost:8888/{application}/{profile}
 *
 * Các service kết nối qua: spring.config.import=optional:configserver:http://localhost:8888
 * Config Server KHÔNG đăng ký vào Eureka (client gọi trực tiếp qua URL).
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}

package kinderland.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Auth Service (port 8081) — DB riêng kinderland_auth_db.
 *
 * Trách nhiệm: đăng ký/đăng nhập (local + Google), quản lý account/address/profile,
 * CẤP PHÁT JWT (access + refresh). Khi đăng ký thành công sẽ bắn UserCreatedEvent
 * lên Kafka topic 'notification-events' để notification-service gửi email chào mừng.
 *
 * @EnableAsync: phục vụ luồng gửi OTP quên mật khẩu (@Async).
 */
@SpringBootApplication
@EnableAsync
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}

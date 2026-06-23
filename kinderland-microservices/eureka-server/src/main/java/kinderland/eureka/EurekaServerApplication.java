package kinderland.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Service Discovery cho hệ thống Kinderland.
 *
 * Tất cả các service (api-gateway, auth-service, product-service, order-service,
 * notification-service) sẽ đăng ký vào đây và tìm nhau qua tên service
 * (vd: lb://AUTH-SERVICE) thay vì hard-code host:port.
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}

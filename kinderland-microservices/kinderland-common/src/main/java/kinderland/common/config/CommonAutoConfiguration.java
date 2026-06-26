package kinderland.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.HandlerExceptionResolver;
import kinderland.common.exception.GlobalExceptionHandler;
import kinderland.common.security.JsonAccessDeniedHandler;
import kinderland.common.security.JsonAuthenticationEntryPoint;

/**
 * Auto-configuration cho kinderland-common.
 *
 * Nhờ class này (được khai báo trong META-INF/spring/.../AutoConfiguration.imports),
 * BẤT KỲ service nào có kinderland-common trên classpath sẽ TỰ ĐỘNG có
 * {@link GlobalExceptionHandler}.
 *
 * Các bean Security (JsonAccessDeniedHandler, JsonAuthenticationEntryPoint) chỉ được
 * khởi tạo nếu service tiêu thụ có sử dụng Spring Security (ConditionalOnClass).
 */
@AutoConfiguration
@Import(GlobalExceptionHandler.class)
public class CommonAutoConfiguration {

    @Configuration
    @ConditionalOnClass(name = "org.springframework.security.web.access.AccessDeniedHandler")
    public static class SecurityHandlerAutoConfiguration {

        @Bean
        public JsonAccessDeniedHandler jsonAccessDeniedHandler(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {
            return new JsonAccessDeniedHandler(handlerExceptionResolver);
        }

        @Bean
        public JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {
            return new JsonAuthenticationEntryPoint(handlerExceptionResolver);
        }
    }
}


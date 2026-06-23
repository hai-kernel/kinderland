package kinderland.common.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;
import kinderland.common.exception.GlobalExceptionHandler;

/**
 * Auto-configuration cho kinderland-common.
 *
 * Nhờ class này (được khai báo trong META-INF/spring/.../AutoConfiguration.imports),
 * BẤT KỲ service nào có kinderland-common trên classpath sẽ TỰ ĐỘNG có
 * {@link GlobalExceptionHandler} mà không cần mở rộng scanBasePackages.
 */
@AutoConfiguration
@Import(GlobalExceptionHandler.class)
public class CommonAutoConfiguration {
}

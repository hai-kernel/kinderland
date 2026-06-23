package kinderland.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security của auth-service.
 *
 * Khác monolith: KHÔNG còn validate JWT ở đây (Gateway lo). Thay vào đó dùng
 * {@link HeaderAuthenticationFilter} để dựng SecurityContext từ header Gateway.
 *
 * permitAll ở tầng filter chain vì Gateway đã chặn path protected; việc phân quyền
 * chi tiết theo role vẫn do @PreAuthorize tại controller đảm nhận (EnableMethodSecurity).
 *
 * Vẫn cần PasswordEncoder + AuthenticationManager + CustomUserDetailsService để xử lý
 * ĐĂNG NHẬP LOCAL (login/password) — đây là việc của riêng auth-service.
 *
 * CORS do Gateway xử lý ⇒ KHÔNG cấu hình CORS ở đây để tránh trùng header.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                .addFilterBefore(new HeaderAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

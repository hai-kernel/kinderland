package kinderland.product.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for product-service.
 *
 * Authentication is centralized at the API Gateway (JWT verification + header injection).
 * This config adds a second layer of defense:
 *  - All GET requests to public resource paths are permitted without a token.
 *  - All write operations (POST/PUT/DELETE/PATCH) require an authenticated principal
 *    (i.e., the Gateway must have injected X-Auth-Email).
 *  - Admin-only operations are further guarded by @PreAuthorize("hasAuthority('ROLE_ADMIN')")
 *    on the individual controller methods.
 *
 * CORS is disabled here — handled centrally at the Gateway.
 *
 * exceptionHandling: Custom handlers ensure a proper BaseResponse JSON body is returned
 * for 401 (no/invalid token) and 403 (authenticated but insufficient role) instead of
 * Spring Security's default empty white-label response.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private AccessDeniedHandler accessDeniedHandler;

    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Internal Feign calls between services — no auth required
                        .requestMatchers("/internal/**").permitAll()
                        // Swagger / OpenAPI docs — always public
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Public read access for storefront browsing
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/products/**",
                                "/api/v1/categories/**",
                                "/api/v1/brands/**",
                                "/api/v1/sku/**",
                                "/api/v1/stores/**",
                                "/api/v1/inventory/**",
                                "/api/v1/blogs/**",
                                "/api/v1/blog-categories/**",
                                "/api/v1/reviews/**",
                                "/api/v1/promotions/**"
                        ).permitAll()
                        // All write operations require authentication — role check done by @PreAuthorize
                        .anyRequest().authenticated()
                )
                // Return proper JSON body on 401 (no token) and 403 (insufficient role)
                // using shared beans from kinderland-common
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .addFilterBefore(new HeaderAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

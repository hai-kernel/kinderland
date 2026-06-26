package kinderland.gateway.filter;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import kinderland.gateway.security.JwtService;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Global filter xác thực TẬP TRUNG cho toàn hệ thống.
 *
 * Thay thế cho AuthTokenFilter của monolith. Luồng xử lý:
 *  1. Bỏ (strip) mọi header danh tính do client gửi lên (chống giả mạo X-Auth-*).
 *  2. Nếu có Bearer token: validate. Token hợp lệ -> gắn X-Auth-Email / X-Auth-Role
 *     để service downstream tin dùng (không cần parse lại JWT).
 *  3. Path PUBLIC -> luôn cho qua (kể cả không có / token hỏng), nhưng vẫn truyền
 *     danh tính nếu token hợp lệ (hữu ích cho endpoint vừa cho khách vừa cho user).
 *  4. Path PROTECTED mà thiếu / token không hợp lệ -> trả 401 ngay tại Gateway.
 *
 * Việc phân quyền theo ROLE (hasRole) vẫn nằm ở từng service qua @PreAuthorize,
 * dựa trên header X-Auth-Role mà Gateway đã đặt.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationGlobalFilter implements GlobalFilter, Ordered {

    public static final String HEADER_EMAIL = "X-Auth-Email";
    public static final String HEADER_ROLE = "X-Auth-Role";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * Danh sách path KHÔNG yêu cầu đăng nhập (công khai cho mọi HTTP method).
     */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/**",
            "/api/v1/welcome",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api/v1/payment/vnpay-return",
            "/api/v1/payment/verify-vnpay"
    );

    /**
     * Danh sách path CHỈ công khai với HTTP GET (cho phép xem danh sách/chi tiết không cần đăng nhập).
     * Các method khác (POST, PUT, DELETE, PATCH...) BẮT BUỘC phải có token xác thực.
     */
    private static final List<String> PUBLIC_GET_PATHS = List.of(
            "/api/v1/categories/**",
            "/api/v1/inventory/**",
            "/api/v1/brands/**",
            "/api/v1/sku/**",
            "/api/v1/stores/**",
            "/api/v1/products/**",
            "/api/v1/financial/**",
            "/api/v1/blogs/**",
            "/api/v1/blog-categories/**",
            "/api/v1/reviews/**",
            "/api/v1/promotions/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Preflight CORS không mang token -> luôn cho qua (CorsWebFilter sẽ xử lý).
        if (request.getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        String token = resolveToken(request);
        String email = null;
        String role = null;

        if (token != null) {
            try {
                Claims claims = jwtService.parseAccessToken(token);
                email = claims.getSubject();
                role = claims.get("role", String.class);
            } catch (Exception e) {
                log.warn("Invalid JWT token for path {}: {}", path, e.getMessage());
                if (!isPublic(path, request.getMethod())) {
                    return unauthorized(exchange, path, "Invalid or expired token");
                }
            }
        } else if (!isPublic(path, request.getMethod())) {
            return unauthorized(exchange, path, "Authentication token is required to access this resource");
        }

        final String authedEmail = email;
        final String authedRole = role;
        ServerHttpRequest mutatedRequest = request.mutate()
                .headers(headers -> {
                    // Chống giả mạo: xoá header client tự gửi rồi mới gắn lại từ token đã verify.
                    headers.remove(HEADER_EMAIL);
                    headers.remove(HEADER_ROLE);
                    if (authedEmail != null) {
                        headers.add(HEADER_EMAIL, authedEmail);
                    }
                    if (authedRole != null) {
                        headers.add(HEADER_ROLE, authedRole);
                    }
                })
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private boolean isPublic(String path, HttpMethod method) {
        if (PUBLIC_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path))) {
            return true;
        }
        if (method == HttpMethod.GET && PUBLIC_GET_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path))) {
            return true;
        }
        return false;
    }

    private String resolveToken(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String path, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String timestamp = java.time.Instant.now().toString();
        String body = String.format(
                "{\"timestamp\":\"%s\",\"statusCode\":401,\"apiPath\":\"%s\",\"isSuccess\":false,\"message\":\"%s\",\"data\":null}",
                timestamp, path, message
        );
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // Chạy sớm, trước các filter định tuyến.
        return -1;
    }
}

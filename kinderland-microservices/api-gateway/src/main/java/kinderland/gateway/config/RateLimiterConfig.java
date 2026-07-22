package kinderland.gateway.config;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * KeyResolver cho RequestRateLimiter (Redis token bucket).
 *
 * NGUYÊN TẮC:
 *  - Key KHÔNG chứa PII thô (email/IP nguyên bản) -> hash trước khi đưa vào Redis key.
 *  - KHÔNG decode lại JWT: AuthenticationGlobalFilter (order -1) đã verify token và gắn
 *    X-Auth-Email vào request đã mutate, đồng thời XOÁ header client tự gửi nên không giả mạo được.
 *  - Tất cả resolver trả Mono, không block.
 */
@Configuration
public class RateLimiterConfig {

    /** Header do AuthenticationGlobalFilter gắn SAU khi verify JWT. */
    private static final String HEADER_EMAIL = "X-Auth-Email";

    /**
     * Chỉ tin X-Forwarded-For khi Gateway thực sự nằm sau proxy tin cậy.
     * Mặc định TẮT: nếu Gateway public trực tiếp mà tin header này, client tự gửi
     * X-Forwarded-For giả là vượt được mọi giới hạn theo IP.
     */
    @Value("${gateway.trusted-proxies.enabled:false}")
    private boolean trustedProxyEnabled;

    @Value("${gateway.trusted-proxies.addresses:}")
    private String trustedProxyAddresses;

    /**
     * Key theo NGƯỜI DÙNG đã xác thực.
     * Chưa đăng nhập -> tách theo IP, KHÔNG dồn hết vào một key "anonymous"
     * (nếu dồn chung, một người có thể tiêu hết quota của tất cả khách vãng lai).
     */
    @Bean
    public KeyResolver authenticatedUserKeyResolver() {
        return exchange -> {
            String email = exchange.getRequest().getHeaders().getFirst(HEADER_EMAIL);
            if (StringUtils.hasText(email)) {
                return Mono.just("user:" + hash(email.trim().toLowerCase()));
            }
            return Mono.just("anon:ip:" + hash(resolveClientIp(exchange)));
        };
    }

    /** Key theo IP client. Dùng cho route public (không cần đăng nhập). */
    @Bean
    public KeyResolver clientIpKeyResolver() {
        return exchange -> Mono.just("ip:" + hash(resolveClientIp(exchange)));
    }

    /**
     * Key cho các endpoint auth nhạy cảm (login/register/forgot-password).
     *
     * CHỦ Ý dùng IP, KHÔNG đọc request body để lấy email: đọc body trong WebFlux mà không
     * cache/khôi phục đúng cách sẽ làm auth-service nhận request rỗng. Rủi ro đó lớn hơn
     * lợi ích của việc tách key theo email. Có thể nâng cấp sau bằng body-caching filter.
     */
    @Bean
    public KeyResolver loginKeyResolver() {
        return exchange -> Mono.just("login:ip:" + hash(resolveClientIp(exchange)));
    }

    /**
     * Lấy IP client an toàn.
     * Mặc định dùng remoteAddress. Chỉ đọc X-Forwarded-For khi bật trusted proxy
     * VÀ remoteAddress nằm trong danh sách proxy tin cậy.
     */
    private String resolveClientIp(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        InetSocketAddress remote = request.getRemoteAddress();
        String remoteIp = (remote == null || remote.getAddress() == null)
                ? "unknown"
                : remote.getAddress().getHostAddress();

        if (trustedProxyEnabled && isTrustedProxy(remoteIp)) {
            String xff = request.getHeaders().getFirst("X-Forwarded-For");
            if (StringUtils.hasText(xff)) {
                // X-Forwarded-For: client, proxy1, proxy2 -> phần tử ĐẦU là client gốc.
                String first = xff.split(",")[0].trim();
                if (StringUtils.hasText(first)) {
                    return normalize(first);
                }
            }
        }
        return normalize(remoteIp);
    }

    private boolean isTrustedProxy(String ip) {
        if (!StringUtils.hasText(trustedProxyAddresses)) {
            return false;
        }
        Set<String> trusted = new HashSet<>(Arrays.asList(trustedProxyAddresses.split(",")));
        return trusted.stream().map(String::trim).anyMatch(t -> t.equals(ip));
    }

    /** Chuẩn hoá IPv6-mapped IPv4 (::ffff:127.0.0.1 -> 127.0.0.1) và bỏ port nếu lẫn vào. */
    private String normalize(String ip) {
        if (ip == null) {
            return "unknown";
        }
        String v = ip.trim();
        if (v.startsWith("::ffff:")) {
            v = v.substring("::ffff:".length());
        }
        if (v.equals("0:0:0:0:0:0:0:1")) {
            v = "::1";
        }
        return v;
    }

    /**
     * Hash key để Redis KHÔNG lưu email/IP thô (giảm rủi ro lộ PII khi dump Redis hoặc log).
     * SHA-256 rút gọn 16 hex là đủ tránh va chạm cho mục đích rate limit.
     */
    private String hash(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02x", d[i]));
            }
            return sb.toString();
        } catch (Exception e) {
            return "na";
        }
    }
}

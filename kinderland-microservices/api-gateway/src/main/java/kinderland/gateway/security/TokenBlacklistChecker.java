package kinderland.gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Kiểm tra token có nằm trong blacklist (Redis) hay không.
 *
 * auth-service ghi key "blacklist:&lt;token&gt;" vào Redis khi user logout (TTL = hạn còn lại của token).
 * Gateway chỉ cần hỏi Redis xem key đó có tồn tại không — cực nhanh, KHÔNG gọi HTTP sang auth-service.
 *
 * Dùng bản REACTIVE vì Gateway chạy trên WebFlux (non-blocking).
 */
@Component
@RequiredArgsConstructor
public class TokenBlacklistChecker {

    /** PHẢI trùng prefix mà auth-service dùng khi ghi. */
    private static final String KEY_PREFIX = "blacklist:";

    private final ReactiveStringRedisTemplate redisTemplate;

    /** true nếu token đã bị thu hồi (đang nằm trong blacklist). */
    public Mono<Boolean> isBlacklisted(String token) {
        return redisTemplate.hasKey(KEY_PREFIX + token)
                // Nếu Redis lỗi/không kết nối được -> coi như KHÔNG blacklist để không chặn nhầm traffic hợp lệ.
                .onErrorReturn(false);
    }
}

package kinderland.auth.service;

import kinderland.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Quản lý danh sách token bị thu hồi (blacklist) bằng Redis.
 *
 * Vì sao Redis mà không phải bảng SQL?
 *  - api-gateway cần check blacklist trên MỌI request, phải cực nhanh và KHÔNG nên gọi HTTP
 *    sang auth-service (tránh biến auth-service thành bottleneck). Redis là store dùng chung,
 *    đọc/ghi in-memory nên rất nhanh.
 *  - Redis tự động xoá key khi hết TTL, nên ta đặt TTL = thời gian còn lại của token:
 *    khi token hết hạn (Gateway tự chặn bằng verify chữ ký) thì entry cũng biến mất, không rác.
 *
 * Key: "blacklist:&lt;token&gt;". Gateway dùng ĐÚNG prefix này để tra cứu.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    /** PHẢI trùng với prefix mà api-gateway dùng khi tra cứu. */
    private static final String KEY_PREFIX = "blacklist:";

    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;

    /** Đưa token vào blacklist với TTL = thời gian còn lại của chính token đó. */
    public void blacklist(String token) {
        Duration ttl = remainingTtl(token);
        if (ttl.isZero() || ttl.isNegative()) {
            // Token đã hết hạn -> Gateway tự chặn rồi, không cần lưu.
            return;
        }
        redisTemplate.opsForValue().set(KEY_PREFIX + token, "1", ttl);
    }

    private Duration remainingTtl(String token) {
        try {
            return Duration.between(Instant.now(), jwtUtil.getExpiration(token));
        } catch (Exception e) {
            // Token không parse được (hiếm khi tới đây vì đã qua Gateway) -> fallback an toàn 1 giờ.
            log.warn("Không đọc được hạn token khi blacklist, dùng TTL mặc định 1h: {}", e.getMessage());
            return Duration.ofHours(1);
        }
    }
}

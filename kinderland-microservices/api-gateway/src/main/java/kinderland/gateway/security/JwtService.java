package kinderland.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * Validate & đọc claim từ JWT tại Gateway.
 *
 * Logic được giữ GIỐNG HỆT {@code JwtUtil} của monolith (kinderland-core):
 *  - cùng thuật toán HMAC-SHA (Keys.hmacShaKeyFor),
 *  - cùng secret (biến môi trường JWT_SECRET),
 *  - cùng quy ước claim: subject = email, claim "role", claim "tokenType" = "access".
 * Nhờ vậy token do auth-service phát ra sẽ validate thành công ở đây.
 *
 * Gateway CHỈ validate access token (không xử lý refresh token — việc đó thuộc auth-service).
 */
@Component
public class JwtService {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "access";

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Parse & verify chữ ký token, đồng thời bắt buộc đây là access token.
     * Ném exception nếu token sai chữ ký / hết hạn / không phải access token.
     */
    public Claims parseAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (!ACCESS_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new IllegalArgumentException("Token is not an access token");
        }
        return claims;
    }
}

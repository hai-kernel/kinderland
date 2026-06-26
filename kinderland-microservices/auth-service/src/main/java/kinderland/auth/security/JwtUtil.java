package kinderland.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;

/**
 * Sinh & đọc JWT. CHỈ còn ở auth-service (service duy nhất phát hành token).
 * Việc VERIFY token cho mọi request đã chuyển về API Gateway.
 *
 * Giữ nguyên thuật toán/định dạng claim như monolith để token tương thích với Gateway:
 * subject = email, claim "role", claim "tokenType" ∈ {access, refresh}.
 */
@Component
@Slf4j
public class JwtUtil {
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    @Value("${jwt.refresh-expiration:604800000}")
    private int jwtRefreshExpirationMs;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String email, String role) {
        return Jwts.builder()
                .claim("role", role)
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(String email, String role) {
        return Jwts.builder()
                .claim("role", role)
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtRefreshExpirationMs))
                .signWith(secretKey)
                .compact();
    }

    public String generateAccessTokenFromRefreshToken(String refreshToken) {
        if (!validateRefreshToken(refreshToken)) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        return generateToken(getEmailFromToken(refreshToken), getRoleFromToken(refreshToken));
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }

    public boolean validateRefreshToken(String token) {
        try {
            return REFRESH_TOKEN_TYPE.equals(getClaims(token).get(TOKEN_TYPE_CLAIM, String.class));
        } catch (Exception e) {
            log.error("Invalid JWT refresh token: {}", e.getMessage());
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

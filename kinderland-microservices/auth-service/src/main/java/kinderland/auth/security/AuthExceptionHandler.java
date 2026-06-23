package kinderland.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import kinderland.common.dto.BaseResponse;

/**
 * Handler riêng cho lỗi đăng nhập (BadCredentialsException) — chỉ phát sinh ở auth-service,
 * nên không đặt trong GlobalExceptionHandler dùng chung (tránh kéo spring-security vào common).
 */
@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<BaseResponse<Object>> handleBadCredentials(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(BaseResponse.error(HttpStatus.UNAUTHORIZED.value(), request.getRequestURI(),
                        "Invalid Password", null));
    }
}

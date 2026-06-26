package kinderland.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;

import java.io.IOException;

/**
 * Forwards 403 Forbidden exceptions from Spring Security filter chain
 * to GlobalExceptionHandler by throwing AppException via HandlerExceptionResolver.
 */
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private final HandlerExceptionResolver resolver;

    public JsonAccessDeniedHandler(HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        resolver.resolveException(request, response, null, new AppException(ErrorCode.UNAUTHORIZED_ACCESS));
    }
}

package kinderland.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.servlet.HandlerExceptionResolver;
import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;

import java.io.IOException;

/**
 * Forwards 401 Unauthorized exceptions from Spring Security filter chain
 * to GlobalExceptionHandler by throwing AppException via HandlerExceptionResolver.
 */
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final HandlerExceptionResolver resolver;

    public JsonAuthenticationEntryPoint(HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        resolver.resolveException(request, response, null, new AppException(ErrorCode.MISSING_TOKEN));
    }
}

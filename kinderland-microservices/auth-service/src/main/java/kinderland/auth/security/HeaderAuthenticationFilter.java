package kinderland.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import kinderland.common.security.GatewayAuthContext;

import java.io.IOException;
import java.util.List;

/**
 * Dựng lại SecurityContext từ header do API Gateway truyền xuống (X-Auth-Email / X-Auth-Role).
 *
 * Thay cho AuthTokenFilter của monolith: service KHÔNG còn tự parse JWT nữa — Gateway đã verify
 * và đặt header tin cậy. Nhờ filter này, các code cũ dùng SecurityContextHolder.getName()
 * và @PreAuthorize("hasAuthority('ROLE_ADMIN')") vẫn hoạt động nguyên vẹn.
 *
 * CỐ Ý không đánh dấu @Component: được khởi tạo trực tiếp trong SecurityConfig để tránh
 * Spring Boot tự đăng ký nó thành servlet filter top-level (đăng ký kép).
 */
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String email = request.getHeader(GatewayAuthContext.HEADER_EMAIL);
        String role = request.getHeader(GatewayAuthContext.HEADER_ROLE);

        if (StringUtils.hasText(email) && SecurityContextHolder.getContext().getAuthentication() == null) {
            List<SimpleGrantedAuthority> authorities = StringUtils.hasText(role)
                    ? List.of(new SimpleGrantedAuthority(role))
                    : List.of();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}

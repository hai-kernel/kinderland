package kinderland.product.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kinderland.common.security.GatewayAuthContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Rebuilds Spring SecurityContext from the X-Auth-Email / X-Auth-Role headers
 * that the API Gateway injects after verifying the JWT.
 *
 * This service does NOT parse JWT itself — it trusts the Gateway-injected headers.
 * Enables @PreAuthorize("hasAuthority('ROLE_ADMIN')") on controller write endpoints.
 *
 * Not annotated with @Component intentionally: registered directly in SecurityConfig
 * to prevent Spring Boot from auto-registering it as a top-level servlet filter.
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

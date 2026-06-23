package kinderland.common.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Util đọc danh tính người dùng từ header do API Gateway gắn vào SAU KHI đã verify JWT.
 *
 * Trong kiến trúc này, các service nội bộ KHÔNG tự parse JWT — chúng tin tưởng các header
 * X-Auth-Email / X-Auth-Role mà Gateway truyền xuống (Gateway đã strip header giả mạo từ client).
 *
 * Tiện cho service muốn lấy nhanh email/role mà không cần đụng SecurityContext.
 */
public final class GatewayAuthContext {

    public static final String HEADER_EMAIL = "X-Auth-Email";
    public static final String HEADER_ROLE = "X-Auth-Role";

    private GatewayAuthContext() {
    }

    /** Email (subject của JWT) của user hiện tại, hoặc null nếu request ẩn danh. */
    public static String getCurrentEmail() {
        return header(HEADER_EMAIL);
    }

    /** Role của user hiện tại (vd "ROLE_ADMIN"), hoặc null nếu request ẩn danh. */
    public static String getCurrentRole() {
        return header(HEADER_ROLE);
    }

    public static boolean isAuthenticated() {
        return getCurrentEmail() != null;
    }

    private static String header(String name) {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return null;
        }
        String value = request.getHeader(name);
        return (value == null || value.isBlank()) ? null : value;
    }

    private static HttpServletRequest currentRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            return servletAttrs.getRequest();
        }
        return null;
    }
}

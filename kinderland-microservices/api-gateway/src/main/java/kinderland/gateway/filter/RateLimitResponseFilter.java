package kinderland.gateway.filter;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * RequestRateLimiter mặc định trả 429 với body RỖNG (client nhận trang trắng).
 * Filter này viết body JSON đúng envelope mà Gateway đang dùng ở các lỗi khác
 * ({timestamp, statusCode, apiPath, isSuccess, message, data}) để frontend chỉ phải
 * xử lý MỘT dạng lỗi duy nhất.
 *
 * Chạy SAU cùng (order cao) để thấy được status code do limiter đặt.
 */
@Component
@Slf4j
public class RateLimitResponseFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.defer(() -> {
            ServerHttpResponse response = exchange.getResponse();

            // Chỉ can thiệp khi limiter đã đặt 429 và body CHƯA được ghi.
            if (!HttpStatus.TOO_MANY_REQUESTS.equals(response.getStatusCode()) || response.isCommitted()) {
                return Mono.empty();
            }

            String path = exchange.getRequest().getPath().value();
            String requestId = exchange.getRequest().getId();
            Object routeId = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR) == null
                    ? "unknown"
                    : String.valueOf(exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR));

            // Log có cấu trúc: KHÔNG log key rate limit, IP, email hay body.
            log.warn("Rate limit exceeded -> status=429 method={} path={} requestId={}",
                    exchange.getRequest().getMethod(), path, requestId);

            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            String body = String.format(
                    "{\"timestamp\":\"%s\",\"statusCode\":429,\"apiPath\":\"%s\",\"isSuccess\":false,"
                            + "\"code\":\"RATE_LIMIT_EXCEEDED\","
                            + "\"message\":\"Bạn gửi quá nhiều yêu cầu. Vui lòng thử lại sau.\","
                            + "\"requestId\":\"%s\",\"data\":null}",
                    Instant.now(), path, requestId);

            DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        }));
    }

    @Override
    public int getOrder() {
        // Sau routing/limiter để đọc được status code cuối cùng.
        return Ordered.LOWEST_PRECEDENCE;
    }
}

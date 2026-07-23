package kinderland.gateway.filter;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * RequestRateLimiter thay thế, có body JSON khi bị từ chối.
 *
 * VÌ SAO KHÔNG DÙNG BẢN CHUẨN + filter ghi body sau:
 * RequestRateLimiterGatewayFilterFactory đặt status 429 rồi gọi response.setComplete().
 * Response committed ngay tại đó, nên mọi filter chạy sau chain.filter() không thể ghi body,
 * và ServerHttpResponseDecorator override writeWith() cũng không bắt được vì
 * setComplete() KHÔNG đi qua writeWith().
 *
 * Filter này tự gọi RedisRateLimiter.isAllowed() và ghi JSON NGAY khi bị từ chối,
 * lúc response còn chưa committed.
 *
 * KHÔNG rewrite 429 do downstream trả về: filter chỉ tạo 429 khi CHÍNH limiter từ chối,
 * và khi đó nó không gọi chain.filter() nên downstream không hề được gọi.
 */
@Component
@Slf4j
public class JsonRequestRateLimiterGatewayFilterFactory
        extends AbstractGatewayFilterFactory<JsonRequestRateLimiterGatewayFilterFactory.Config> {

    private final RedisRateLimiter redisRateLimiter;
    private final KeyResolver defaultKeyResolver;

    public JsonRequestRateLimiterGatewayFilterFactory(RedisRateLimiter redisRateLimiter,
                                                      KeyResolver defaultKeyResolver) {
        super(Config.class);
        this.redisRateLimiter = redisRateLimiter;
        this.defaultKeyResolver = defaultKeyResolver;
    }

    @Getter
    @Setter
    public static class Config {
        private int replenishRate = 60;
        private int burstCapacity = 100;
        private int requestedTokens = 1;
        /** KeyResolver tuỳ chọn: "#{@authenticatedUserKeyResolver}". Bỏ trống -> dùng bean @Primary. */
        private KeyResolver keyResolver;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // OPTIONS preflight KHÔNG tiêu tốn token: trình duyệt tự gửi, không phải hành vi người dùng.
            if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
                return chain.filter(exchange);
            }

            Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            String routeId = route != null ? route.getId() : "unknown";

            // Đăng ký quota cho routeId này (RedisRateLimiter tra config theo routeId).
            redisRateLimiter.getConfig().computeIfAbsent(routeId, id -> new RedisRateLimiter.Config()
                    .setReplenishRate(config.getReplenishRate())
                    .setBurstCapacity(config.getBurstCapacity())
                    .setRequestedTokens(config.getRequestedTokens()));

            KeyResolver resolver = config.getKeyResolver() != null ? config.getKeyResolver() : defaultKeyResolver;

            return resolver.resolve(exchange)
                    .defaultIfEmpty("")
                    .flatMap(key -> {
                        if (!StringUtils.hasText(key)) {
                            // Không resolve được key -> KHÔNG bypass im lặng.
                            log.warn("Rate limiter: khong resolve duoc key, ap dung key chung. routeId={}", routeId);
                            key = "unresolved";
                        }
                        return redisRateLimiter.isAllowed(routeId, key);
                    })
                    .flatMap(response -> {
                        // Header do framework tính (remaining/replenish/burst) - giá trị thật, không bịa.
                        response.getHeaders()
                                .forEach((k, v) -> exchange.getResponse().getHeaders().add(k, v));

                        if (response.isAllowed()) {
                            return chain.filter(exchange);
                        }
                        return writeTooManyRequests(exchange, routeId);
                    })
                    .onErrorResume(e -> {
                        // Redis lỗi -> FAIL-OPEN cho route này, có log + KHÔNG nuốt im lặng.
                        // KHÔNG trả 429 vì không biết quota đã vượt hay chưa.
                        log.warn("Rate limiter: Redis loi -> fail-open. routeId={} error={}",
                                routeId, e.getClass().getSimpleName());
                        return chain.filter(exchange);
                    });
        };
    }

    private Mono<Void> writeTooManyRequests(ServerWebExchange exchange, String routeId) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.empty();
        }

        String path = exchange.getRequest().getPath().value();
        String requestId = exchange.getRequest().getId();

        // Log có cấu trúc; KHÔNG log key/IP/email/JWT.
        log.warn("Rate limit exceeded -> status=429 routeId={} method={} path={} requestId={}",
                routeId, exchange.getRequest().getMethod(), path, requestId);

        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
                "{\"timestamp\":\"%s\",\"statusCode\":429,\"apiPath\":\"%s\",\"isSuccess\":false,"
                        + "\"code\":\"RATE_LIMIT_EXCEEDED\","
                        + "\"message\":\"Bạn gửi quá nhiều yêu cầu. Vui lòng thử lại sau.\","
                        + "\"requestId\":\"%s\",\"data\":null}",
                Instant.now(), path, requestId);

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public String name() {
        return "JsonRequestRateLimiter";
    }
}

package kinderland.gateway.controller;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Fallback khi CircuitBreaker mở hoặc downstream timeout.
 *
 * Reactive (Mono), không blocking. Trả HTTP 503 + JSON, KHÔNG redirect, KHÔNG trả HTML.
 *
 * Envelope PHẢI trùng shape mà AuthenticationGlobalFilter.unauthorized() đang trả
 * ({timestamp, statusCode, apiPath, isSuccess, message, data}) để frontend chỉ phải
 * xử lý một dạng lỗi duy nhất từ Gateway.
 *
 * LƯU Ý: dùng Map với key "isSuccess" tường minh thay vì BaseResponse — Lombok sinh
 * getter isSuccess() nên Jackson sẽ serialize thành "success", tạo ra shape thứ ba.
 */
@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    private static final String CODE = "SERVICE_TEMPORARILY_UNAVAILABLE";
    private static final String MESSAGE = "Dịch vụ tạm thời không khả dụng. Vui lòng thử lại sau.";

    @RequestMapping("/auth")
    public Mono<ResponseEntity<Map<String, Object>>> auth(ServerWebExchange exchange) {
        return build("auth-service", exchange);
    }

    @RequestMapping("/products")
    public Mono<ResponseEntity<Map<String, Object>>> products(ServerWebExchange exchange) {
        return build("product-service", exchange);
    }

    @RequestMapping("/orders")
    public Mono<ResponseEntity<Map<String, Object>>> orders(ServerWebExchange exchange) {
        return build("order-service", exchange);
    }

    /**
     * Fallback payment: TUYỆT ĐỐI không trả paymentUrl giả, không isSuccess=true,
     * không ám chỉ giao dịch đã thành công. Frontend phải hiểu là CHƯA tạo được thanh toán.
     */
    @RequestMapping("/payments")
    public Mono<ResponseEntity<Map<String, Object>>> payments(ServerWebExchange exchange) {
        return build("payment-service", exchange);
    }

    private Mono<ResponseEntity<Map<String, Object>>> build(String service, ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();
        String requestId = exchange.getRequest().getId();

        // Log để quan sát; KHÔNG log Authorization/JWT/query nhạy cảm.
        log.warn("Gateway fallback -> service={} method={} path={} requestId={} reason=circuit-open-or-timeout",
                service, method, path, requestId);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("statusCode", HttpStatus.SERVICE_UNAVAILABLE.value());
        body.put("apiPath", path);
        body.put("isSuccess", false);
        body.put("code", CODE);
        body.put("message", MESSAGE);
        body.put("service", service);
        body.put("requestId", requestId);
        body.put("data", null);

        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body));
    }
}

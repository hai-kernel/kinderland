package kinderland.order.client;

import kinderland.order.client.dto.InitiatePaymentRequest;
import kinderland.order.client.dto.PaymentInitResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * OpenFeign client gọi Internal API của Payment Service khi khách checkout.
 * name = "PAYMENT-SERVICE" -> phân giải qua Eureka (không hard-code host:port), KHÔNG qua Gateway.
 */
@FeignClient(name = "PAYMENT-SERVICE", path = "/internal/payments")
public interface PaymentClient {

    @PostMapping("/initiate")
    PaymentInitResponse initiate(@RequestBody InitiatePaymentRequest request);
}

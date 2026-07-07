package kinderland.payment.controller;

import jakarta.validation.Valid;
import kinderland.payment.model.dto.request.InitiatePaymentRequest;
import kinderland.payment.model.dto.response.PaymentInitResponse;
import kinderland.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal API — CHỈ order-service gọi qua Feign (KHÔNG route qua Gateway).
 * Khởi tạo thanh toán cho một đơn khi khách checkout.
 */
@RestController
@RequestMapping("/internal/payments")
@RequiredArgsConstructor
public class PaymentInternalController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public PaymentInitResponse initiate(@Valid @RequestBody InitiatePaymentRequest request) {
        return paymentService.initiate(request);
    }
}

package kinderland.product.controller;

import kinderland.product.model.dto.response.PromotionValidationResponse;
import kinderland.product.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * Internal API — Order Service gọi qua Feign để CHỐT số tiền giảm khi tạo đơn và ghi nhận
 * lượt dùng sau khi thanh toán thành công. KHÔNG route qua Gateway.
 *
 * Trả về DTO trần (không bọc BaseResponse) cho khớp quy ước của các internal controller khác,
 * tránh order-service phải bóc thêm một tầng `data`.
 */
@RestController
@RequestMapping("/internal/promotions")
@RequiredArgsConstructor
public class PromotionInternalController {

    private final PromotionService promotionService;

    @GetMapping("/validate")
    public PromotionValidationResponse validate(@RequestParam String code,
                                                @RequestParam(defaultValue = "0") BigDecimal subtotal) {
        return promotionService.validate(code, subtotal);
    }

    /** Ghi nhận 1 lượt dùng. Trả true nếu ghi nhận được, false nếu mã vừa hết lượt. */
    @PostMapping("/{id}/redeem")
    public boolean redeem(@PathVariable Long id) {
        return promotionService.redeem(id);
    }
}

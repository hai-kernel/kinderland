package kinderland.order.controller;

import jakarta.servlet.http.HttpServletRequest;
import kinderland.common.dto.BaseResponse;
import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.common.security.GatewayAuthContext;
import kinderland.order.model.dto.response.LoyaltyPointsResponse;
import kinderland.order.service.LoyaltyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    /** Điểm tích luỹ của khách hàng hiện tại. */
    @GetMapping("/my-points")
    public ResponseEntity<BaseResponse<LoyaltyPointsResponse>> getMyPoints(HttpServletRequest req) {
        LoyaltyPointsResponse points = loyaltyService.getMyPoints(currentEmail());
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(),
                "Loyalty points retrieved successfully", points));
    }

    private String currentEmail() {
        String email = GatewayAuthContext.getCurrentEmail();
        if (email == null) {
            throw new AppException(ErrorCode.MISSING_TOKEN);
        }
        return email;
    }
}

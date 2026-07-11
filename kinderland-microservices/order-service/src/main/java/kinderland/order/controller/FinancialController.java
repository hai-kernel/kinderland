package kinderland.order.controller;

import jakarta.servlet.http.HttpServletRequest;
import kinderland.common.dto.BaseResponse;
import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.common.security.GatewayAuthContext;
import kinderland.order.model.dto.response.FinancialOverviewResponse;
import kinderland.order.service.FinancialService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Báo cáo tài chính (khớp FE financialApi.ts). Dữ liệu nhạy cảm → chỉ ADMIN/MANAGER
 * (kiểm tra thủ công qua GatewayAuthContext, giống các controller khác của order-service).
 */
@RestController
@RequestMapping("/api/v1/financial")
@RequiredArgsConstructor
public class FinancialController {

    private static final Set<String> STAFF_ROLES = Set.of("ROLE_ADMIN", "ROLE_MANAGER");

    private final FinancialService financialService;

    @GetMapping("/overview")
    public ResponseEntity<BaseResponse<FinancialOverviewResponse>> getOverview(HttpServletRequest req) {
        requireStaff();
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(),
                "Overview successful", financialService.getOverview()));
    }

    @GetMapping("/revenue")
    public ResponseEntity<BaseResponse<BigDecimal>> getRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            HttpServletRequest req) {
        requireStaff();
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(),
                "Get all revenue", financialService.getRevenueByDateRange(start, end)));
    }

    private void requireStaff() {
        String email = GatewayAuthContext.getCurrentEmail();
        if (email == null) {
            throw new AppException(ErrorCode.MISSING_TOKEN);
        }
        if (!STAFF_ROLES.contains(GatewayAuthContext.getCurrentRole())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }
}

package kinderland.order.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kinderland.common.dto.BaseResponse;
import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.common.security.GatewayAuthContext;
import kinderland.order.model.dto.request.RefundRequestDTO;
import kinderland.order.model.dto.request.RejectReturnRequestDTO;
import kinderland.order.model.dto.request.ReturnRequestDTO;
import kinderland.order.model.dto.response.ReturnResponseDTO;
import kinderland.order.service.ReturnRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * Yêu cầu trả hàng (khớp FE returnApi.ts). order-service không dùng @PreAuthorize —
 * kiểm tra quyền thủ công qua GatewayAuthContext (giống OrderController).
 * Hành động của nhân viên: cho phép ADMIN + MANAGER (mono chỉ MANAGER, nới cho ADMIN thao tác/test).
 */
@RestController
@RequestMapping("/api/v1/return-requests")
@RequiredArgsConstructor
public class ReturnRequestController {

    private static final Set<String> STAFF_ROLES = Set.of("ROLE_ADMIN", "ROLE_MANAGER");

    private final ReturnRequestService returnRequestService;

    @GetMapping
    public ResponseEntity<BaseResponse<Page<ReturnResponseDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest req) {
        requireStaff();
        Pageable pageable = PageRequest.of(page, size, Sort.by("requestedAt").descending());
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(),
                "Return requests retrieved successfully", returnRequestService.getAllReturnRequests(pageable)));
    }

    @GetMapping("/my-requests")
    public ResponseEntity<BaseResponse<Page<ReturnResponseDTO>>> myRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest req) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("requestedAt").descending());
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(),
                "My return requests retrieved successfully",
                returnRequestService.getMyReturnRequests(pageable, currentEmail())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ReturnResponseDTO>> getById(@PathVariable Long id, HttpServletRequest req) {
        requireAuthenticated();
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(),
                "Return request retrieved successfully", returnRequestService.getReturnRequestById(id)));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<ReturnResponseDTO>> create(
            @Valid @RequestBody ReturnRequestDTO request, HttpServletRequest req) {
        ReturnResponseDTO result = returnRequestService.createReturnRequest(request, currentEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.ok(201, req.getRequestURI(),
                "Return request created successfully", result));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<BaseResponse<ReturnResponseDTO>> approve(@PathVariable Long id, HttpServletRequest req) {
        requireStaff();
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(),
                "Return request approved successfully", returnRequestService.approveReturn(id, currentEmail())));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<BaseResponse<ReturnResponseDTO>> reject(
            @PathVariable Long id, @Valid @RequestBody RejectReturnRequestDTO request, HttpServletRequest req) {
        requireStaff();
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(),
                "Return request rejected successfully", returnRequestService.rejectReturn(id, request, currentEmail())));
    }

    @PatchMapping("/{id}/receive")
    public ResponseEntity<BaseResponse<ReturnResponseDTO>> receive(@PathVariable Long id, HttpServletRequest req) {
        requireStaff();
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(),
                "Return request marked as received successfully", returnRequestService.markReceived(id, currentEmail())));
    }

    @PatchMapping("/{id}/refund")
    public ResponseEntity<BaseResponse<ReturnResponseDTO>> refund(
            @PathVariable Long id, @Valid @RequestBody RefundRequestDTO request, HttpServletRequest req) {
        requireStaff();
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(),
                "Refund processed successfully", returnRequestService.refund(id, request, currentEmail())));
    }

    /** Nhãn vận chuyển trả hàng (HTML) — khách in dán lên gói, hoặc quản lý xem. */
    @GetMapping(value = "/{id}/label", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getShippingLabel(@PathVariable Long id) {
        requireAuthenticated();
        return ResponseEntity.ok(returnRequestService.generateShippingLabel(id));
    }

    // ---------- auth helpers ----------

    private String currentEmail() {
        String email = GatewayAuthContext.getCurrentEmail();
        if (email == null) {
            throw new AppException(ErrorCode.MISSING_TOKEN);
        }
        return email;
    }

    private void requireAuthenticated() {
        currentEmail();
    }

    private void requireStaff() {
        requireAuthenticated();
        if (!STAFF_ROLES.contains(GatewayAuthContext.getCurrentRole())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }
}

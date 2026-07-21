package kinderland.product.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kinderland.common.dto.BaseResponse;
import kinderland.product.model.dto.request.TransferCreateRequestDTO;
import kinderland.product.model.dto.response.TransferResponseDTO;
import kinderland.product.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Chuyển kho giữa các cửa hàng (khớp FE StockTransferPage). Chỉ ADMIN/MANAGER.
 * "Cửa hàng của tôi" phân giải theo Store.managerEmail (đăng nhập bằng manager của store).
 */
@RestController
@RequestMapping("/api/v1/transfer")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
public class TransferController {

    private final TransferService transferService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<TransferResponseDTO>>> getMyTransfers(HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Success", transferService.getMyTransfers()));
    }

    @PostMapping("/draft")
    public ResponseEntity<BaseResponse<TransferResponseDTO>> createDraft(
            @Valid @RequestBody TransferCreateRequestDTO dto, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Draft created",
                transferService.createDraft(dto)));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<BaseResponse<Void>> submit(@PathVariable Long id, HttpServletRequest req) {
        transferService.sendRequest(id);
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Submitted for approval", null));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<BaseResponse<Void>> approve(@PathVariable Long id, HttpServletRequest req) {
        transferService.approve(id);
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Approved", null));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<BaseResponse<Void>> reject(@PathVariable Long id, HttpServletRequest req) {
        transferService.reject(id);
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Rejected successfully", null));
    }

    @PostMapping("/{id}/ship")
    public ResponseEntity<BaseResponse<Void>> ship(@PathVariable Long id, HttpServletRequest req) {
        transferService.ship(id);
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Out for delivery", null));
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<BaseResponse<Void>> receive(@PathVariable Long id, HttpServletRequest req) {
        transferService.receive(id);
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Received successfully", null));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<BaseResponse<Void>> complete(@PathVariable Long id, HttpServletRequest req) {
        transferService.complete(id);
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Transfer completed", null));
    }

    @PostMapping("/{id}/lost-damaged")
    public ResponseEntity<BaseResponse<Void>> lostDamaged(@PathVariable Long id, HttpServletRequest req) {
        transferService.lostDamaged(id);
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Marked as lost/damaged", null));
    }
}

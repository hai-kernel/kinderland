package kinderland.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import kinderland.auth.model.dto.request.CreateAccountRequest;
import kinderland.auth.model.dto.response.UserResponse;
import kinderland.auth.service.AccountService;
import kinderland.common.dto.BaseResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AccountService accountService;

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("")
    public ResponseEntity<BaseResponse<List<UserResponse>>> getAllAccounts(HttpServletRequest httpRequest) {
        List<UserResponse> response = accountService.getAllAccounts();
        return ResponseEntity.ok(
                BaseResponse.ok(HttpStatus.OK.value(), httpRequest.getRequestURI(), "Fetched all accounts successfully", response)
        );
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<BaseResponse<UserResponse>> createAccount(
            @Validated @RequestBody CreateAccountRequest request,
            HttpServletRequest httpRequest
    ) {
        UserResponse response = accountService.createAccountByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                BaseResponse.ok(HttpStatus.CREATED.value(), httpRequest.getRequestURI(), "Created new account successfully", response)
        );
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteAccount(
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        accountService.deleteAccount(id);
        return ResponseEntity.ok(
                BaseResponse.ok(HttpStatus.OK.value(), httpRequest.getRequestURI(), "Deleted account successfully", null)
        );
    }
}

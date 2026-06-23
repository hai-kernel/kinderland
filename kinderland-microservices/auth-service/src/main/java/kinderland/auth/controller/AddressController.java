package kinderland.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import kinderland.auth.model.dto.request.AddressRequestDTO;
import kinderland.auth.model.dto.response.AddressResponse;
import kinderland.auth.service.IAddressService;
import kinderland.common.dto.BaseResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/address")
@RequiredArgsConstructor
public class AddressController {
    private final IAddressService addressService;

    @PostMapping("/create")
    public ResponseEntity<BaseResponse<AddressResponse>> createAddress(
            @Validated @RequestBody AddressRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        AddressResponse response = addressService.createAddress(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                BaseResponse.ok(HttpStatus.CREATED.value(), httpRequest.getRequestURI(), "Created new address", response)
        );
    }

    @PutMapping("/update/{addressId}")
    public ResponseEntity<BaseResponse<AddressResponse>> updateAddress(
            @PathVariable Long addressId,
            @Validated @RequestBody AddressRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        AddressResponse response = addressService.updateAddress(addressId, request);
        return ResponseEntity.ok(
                BaseResponse.ok(HttpStatus.OK.value(), httpRequest.getRequestURI(), "Updated address successfully", response)
        );
    }

    @DeleteMapping("/delete/{addressId}")
    public ResponseEntity<BaseResponse<Void>> deleteAddress(
            @PathVariable Long addressId,
            HttpServletRequest httpRequest
    ) {
        addressService.deleteAddress(addressId);
        return ResponseEntity.ok(
                BaseResponse.ok(HttpStatus.OK.value(), httpRequest.getRequestURI(), "Deleted address successfully", null)
        );
    }

    @GetMapping("/my-addresses")
    public ResponseEntity<BaseResponse<List<AddressResponse>>> getMyAddresses(HttpServletRequest httpRequest) {
        List<AddressResponse> response = addressService.getAddressesByAccount();
        return ResponseEntity.ok(
                BaseResponse.ok(HttpStatus.OK.value(), httpRequest.getRequestURI(), "Fetched all addresses successfully", response)
        );
    }
}

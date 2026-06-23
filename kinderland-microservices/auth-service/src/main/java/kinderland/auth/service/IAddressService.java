package kinderland.auth.service;

import kinderland.auth.model.dto.request.AddressRequestDTO;
import kinderland.auth.model.dto.response.AddressResponse;

import java.util.List;

public interface IAddressService {
    AddressResponse createAddress(AddressRequestDTO addressRequestDTO);

    AddressResponse updateAddress(Long addressId, AddressRequestDTO addressRequestDTO);

    void deleteAddress(Long addressId);

    List<AddressResponse> getAddressesByAccount();
}

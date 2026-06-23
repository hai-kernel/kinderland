package kinderland.auth.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import kinderland.auth.model.dto.request.AddressRequestDTO;
import kinderland.auth.model.dto.response.AddressResponse;
import kinderland.auth.mapper.AddressMapper;
import kinderland.auth.model.entity.Account;
import kinderland.auth.model.entity.Address;
import kinderland.auth.repo.AccountRepository;
import kinderland.auth.repo.AddressRepository;
import kinderland.auth.service.IAddressService;
import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AddressServiceImpl implements IAddressService {
    AddressRepository addressRepository;
    AccountRepository accountRepository;
    AddressMapper addressMapper;

    private Account getAccountIdFromJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    @Override
    @Transactional
    public AddressResponse createAddress(AddressRequestDTO dto) {
        Address address = addressMapper.toEntity(dto);
        address.setAccount(getAccountIdFromJwt());
        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Long addressId, AddressRequestDTO dto) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        Account account = getAccountIdFromJwt();
        if (!address.getAccount().equals(account)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        if (StringUtils.hasText(dto.getStreet())) {
            address.setStreet(dto.getStreet());
        }
        if (dto.getProvinceId() != null) {
            address.setProvinceId(dto.getProvinceId());
        }
        if (StringUtils.hasText(dto.getProvinceName())) {
            address.setProvinceName(dto.getProvinceName());
        }
        if (dto.getDistrictId() != null) {
            address.setDistrictId(dto.getDistrictId());
        }
        if (StringUtils.hasText(dto.getDistrictName())) {
            address.setDistrictName(dto.getDistrictName());
        }
        if (dto.getWardId() != null) {
            address.setWardId(dto.getWardId());
        }
        if (StringUtils.hasText(dto.getWardName())) {
            address.setWardName(dto.getWardName());
        }

        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public void deleteAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        Account account = getAccountIdFromJwt();
        if (!address.getAccount().equals(account)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        addressRepository.delete(address);
    }

    @Override
    public List<AddressResponse> getAddressesByAccount() {
        Account account = getAccountIdFromJwt();
        return addressMapper.toResponseList(addressRepository.findByAccount(account));
    }
}

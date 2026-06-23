package kinderland.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import kinderland.auth.model.dto.request.AddressRequestDTO;
import kinderland.auth.model.dto.response.AddressResponse;
import kinderland.auth.model.entity.Address;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AddressMapper {

    @Mapping(target = "fullAddress", expression = "java(buildFullAddress(address))")
    AddressResponse toResponse(Address address);

    List<AddressResponse> toResponseList(List<Address> addresses);

    // account gán riêng từ JWT; addressId do DB sinh.
    @Mapping(target = "addressId", ignore = true)
    @Mapping(target = "account", ignore = true)
    Address toEntity(AddressRequestDTO dto);

    /** Field tổng hợp — MapStruct gọi qua expression ở trên. */
    default String buildFullAddress(Address a) {
        return String.format("%s, %s, %s, %s",
                a.getStreet(), a.getWardName(), a.getDistrictName(), a.getProvinceName());
    }
}

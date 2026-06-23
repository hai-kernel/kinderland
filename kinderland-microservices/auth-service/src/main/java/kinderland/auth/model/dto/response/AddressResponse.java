package kinderland.auth.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private Long addressId;
    private String street;
    private String provinceName;
    private String districtName;
    private String wardName;
    private Integer provinceId;
    private Integer districtId;
    private Integer wardId;
    private String fullAddress;
}

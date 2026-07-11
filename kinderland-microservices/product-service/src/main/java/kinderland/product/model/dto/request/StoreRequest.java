package kinderland.product.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalTime;

@Data
public class StoreRequest {
    @NotBlank(message = "Tên cửa hàng không được rỗng")
    private String name;
    private String code;
    private String address;
    private String phone;
    private String managerName;
    private String managerEmail;
    private Double latitude;
    private Double longitude;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Boolean active;
}

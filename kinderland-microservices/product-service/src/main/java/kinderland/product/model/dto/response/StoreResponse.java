package kinderland.product.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class StoreResponse {
    private Long id;
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
    private LocalDateTime createdAt;
}

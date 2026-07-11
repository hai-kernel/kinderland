package kinderland.product.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

/** Cửa hàng/chi nhánh. Bỏ FK Account (managerName giữ dạng String). */
@Entity
@Table(name = "stores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String code;
    private String address;
    private String phone;
    private String managerName;
    /** Email của quản lý cửa hàng (thay FK Account) — dùng để nhận diện store khi nhập/điều chỉnh kho. */
    private String managerEmail;

    private Double latitude;
    private Double longitude;

    private LocalTime openingTime;
    private LocalTime closingTime;

    @Builder.Default
    private Boolean active = true;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (active == null) {
            active = true;
        }
    }
}

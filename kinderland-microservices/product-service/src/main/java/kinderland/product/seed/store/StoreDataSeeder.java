package kinderland.product.seed.store;

import kinderland.common.seed.AbstractDataSeeder;
import kinderland.product.model.entity.Store;
import kinderland.product.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

/**
 * Seed hệ thống cửa hàng tại Việt Nam.
 *
 * Trước đây không có seeder cho Store: ProductDataSeeder tự tạo tạm một bản ghi
 * "Kinderland Default Store / 123 Kinderland" chỉ để gắn tồn kho. Bản ghi đó
 * không có latitude/longitude/phone/giờ mở cửa, nên trang /stores hiện
 * "null - null", nhóm cửa hàng rơi vào mục "Khác" và bản đồ (mặc định 0,0 hoặc
 * toạ độ rác) trôi sang tận Lào.
 *
 * Chạy @Order(25) — TRƯỚC ProductDataSeeder(30) — để khi seeder sản phẩm gọi
 * storeRepository.findAll().findFirst() thì đã có cửa hàng thật, không sinh
 * thêm bản ghi placeholder nữa.
 *
 * Địa chỉ PHẢI chứa "TP.HCM" / "Hà Nội" / "Đà Nẵng" / "Cần Thơ" / "Hải Phòng":
 * frontend nhóm cửa hàng theo thành phố bằng cách dò chuỗi trong address
 * (extractCityFromAddress ở storeApi.ts).
 *
 * Idempotent: bỏ qua cửa hàng đã tồn tại theo mã (code).
 */
@Component
@Order(25)
@ConditionalOnProperty(name = "app.seed.store.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class StoreDataSeeder extends AbstractDataSeeder {

    private final StoreRepository storeRepository;

    @Override
    public String getName() {
        return "StoreDataSeeder";
    }

    @Override
    @Transactional
    public void seed() {
        logStart();

        List<StoreDef> stores = List.of(
                new StoreDef("Kinderland Nguyễn Huệ", "KL-HCM-01",
                        "72 Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM",
                        // managerEmail để trống: findByManagerEmail() trả Optional nên hai cửa hàng
                        // trùng email quản lý sẽ làm truy vấn đó ném NonUniqueResultException.
                        // manager@kinderland.vn đang gắn với cửa hàng sẵn có — không đụng vào.
                        "(+84) 28-7300-8801", "Nguyễn Thị Lan", null,
                        10.774020, 106.703300, LocalTime.of(8, 0), LocalTime.of(22, 0)),

                new StoreDef("Kinderland Crescent Mall", "KL-HCM-02",
                        "101 Tôn Dật Tiên, Phường Tân Phú, Quận 7, TP.HCM",
                        "(+84) 28-7300-8802", "Trần Minh Khoa", null,
                        10.728690, 106.718700, LocalTime.of(9, 0), LocalTime.of(22, 0)),

                new StoreDef("Kinderland Gò Vấp", "KL-HCM-03",
                        "242 Quang Trung, Phường 10, Quận Gò Vấp, TP.HCM",
                        "(+84) 28-7300-8803", "Lê Thu Hà", null,
                        10.836480, 106.665100, LocalTime.of(8, 30), LocalTime.of(21, 30)),

                new StoreDef("Kinderland Hoàn Kiếm", "KL-HN-01",
                        "25 Lý Thường Kiệt, Phường Phan Chu Trinh, Quận Hoàn Kiếm, Hà Nội",
                        "(+84) 24-7300-8804", "Phạm Quốc Anh", null,
                        21.024500, 105.853300, LocalTime.of(8, 0), LocalTime.of(22, 0)),

                new StoreDef("Kinderland Times City", "KL-HN-02",
                        "458 Minh Khai, Phường Vĩnh Tuy, Quận Hai Bà Trưng, Hà Nội",
                        "(+84) 24-7300-8805", "Đỗ Hương Giang", null,
                        20.995300, 105.867600, LocalTime.of(9, 0), LocalTime.of(22, 0)),

                new StoreDef("Kinderland Đà Nẵng", "KL-DN-01",
                        "255 Hùng Vương, Phường Vĩnh Trung, Quận Thanh Khê, Đà Nẵng",
                        "(+84) 236-7300-8806", "Ngô Văn Bình", null,
                        16.067800, 108.214000, LocalTime.of(8, 0), LocalTime.of(21, 30)),

                new StoreDef("Kinderland Cần Thơ", "KL-CT-01",
                        "1 Đại lộ Hòa Bình, Phường Tân An, Quận Ninh Kiều, Cần Thơ",
                        "(+84) 292-7300-8807", "Huỳnh Mỹ Duyên", null,
                        10.033100, 105.782800, LocalTime.of(8, 0), LocalTime.of(21, 0)),

                new StoreDef("Kinderland Hải Phòng", "KL-HP-01",
                        "15 Lạch Tray, Phường Đằng Giang, Quận Ngô Quyền, Hải Phòng",
                        "(+84) 225-7300-8808", "Vũ Đức Thắng", null,
                        20.845100, 106.688000, LocalTime.of(8, 0), LocalTime.of(21, 0))
        );

        int created = 0;
        int skipped = 0;

        for (StoreDef def : stores) {
            boolean exists = storeRepository.findAll().stream()
                    .anyMatch(store -> def.code.equalsIgnoreCase(store.getCode()));
            if (exists) {
                log.info("Skipped store: {}", def.name);
                skipped++;
                continue;
            }

            storeRepository.save(Store.builder()
                    .name(def.name)
                    .code(def.code)
                    .address(def.address)
                    .phone(def.phone)
                    .managerName(def.managerName)
                    .managerEmail(def.managerEmail)
                    .latitude(def.latitude)
                    .longitude(def.longitude)
                    .openingTime(def.openingTime)
                    .closingTime(def.closingTime)
                    .active(true)
                    .build());

            log.info("Created store: {}", def.name);
            created++;
        }

        logCompleted(created, skipped);
    }

    private record StoreDef(
            String name,
            String code,
            String address,
            String phone,
            String managerName,
            String managerEmail,
            Double latitude,
            Double longitude,
            LocalTime openingTime,
            LocalTime closingTime
    ) {}
}

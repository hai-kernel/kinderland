package kinderland.product.seed.promotion;

import kinderland.common.seed.AbstractDataSeeder;
import kinderland.product.model.entity.Promotion;
import kinderland.product.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Order(50)
@ConditionalOnProperty(name = "app.seed.promotion.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class PromotionDataSeeder extends AbstractDataSeeder {

    private final PromotionRepository promotionRepository;

    @Override
    public String getName() {
        return "PromotionDataSeeder";
    }

    @Override
    @Transactional
    public void seed() {
        logStart();

        LocalDateTime now = LocalDateTime.now();

        List<PromoDef> promos = List.of(
                new PromoDef("SUMMER26", "Chào hè rực rỡ", "Giảm 10% đón hè", new BigDecimal("10.00"), now.minusDays(10), now.plusDays(20)),
                new PromoDef("SCHOOL15", "Bé vui đến trường", "Giảm 15% tựu trường", new BigDecimal("15.00"), now.plusDays(10), now.plusDays(40)),
                new PromoDef("WEEKEND5", "Giảm giá cuối tuần", "Ưu đãi cuối tuần", new BigDecimal("5.00"), now.minusDays(2), now.plusDays(2)),
                new PromoDef("BDAY20", "Sinh nhật Kinderland", "Kỷ niệm sinh nhật", new BigDecimal("20.00"), now.minusDays(5), now.plusDays(5)),
                new PromoDef("BF30", "Black Friday", "Siêu giảm giá Black Friday", new BigDecimal("30.00"), now.plusDays(100), now.plusDays(105)),
                new PromoDef("XMAS25", "Giáng sinh cho bé", "Món quà giáng sinh ý nghĩa", new BigDecimal("25.00"), now.plusDays(130), now.plusDays(150)),
                new PromoDef("KIDS16", "Tết thiếu nhi 1/6", "Vui tết thiếu nhi", new BigDecimal("15.00"), now.minusDays(60), now.minusDays(30)),
                new PromoDef("WELCOME10", "Khuyến mãi thành viên mới", "Ưu đãi khách hàng mới", new BigDecimal("10.00"), now.minusDays(365), now.plusDays(365)),
                new PromoDef("COMBO12", "Combo đồ chơi giáo dục", "Giảm giá đặc biệt cho combo", new BigDecimal("12.00"), now.minusDays(10), now.plusDays(30)),
                new PromoDef("FREESHIP", "Miễn phí vận chuyển", "Freeship mọi đơn", new BigDecimal("0.00"), now.minusDays(5), now.plusDays(15)),
                new PromoDef("FLASH12H", "Flash Sale 12 giờ", "Flash sale siêu tốc", new BigDecimal("40.00"), now, now.plusHours(12)),
                new PromoDef("LOYAL20", "Ưu đãi khách hàng thân thiết", "Chương trình loyal", new BigDecimal("20.00"), now.minusDays(100), now.plusDays(100))
        );

        int created = 0;
        int skipped = 0;

        for (PromoDef def : promos) {
            if (promotionRepository.existsByCode(def.code)) {
                skipped++;
                continue;
            }

            Promotion promotion = Promotion.builder()
                    .code(def.code)
                    .title(def.title)
                    .description(def.description)
                    .discountPercent(def.discountPercent)
                    .startDate(def.startDate)
                    .endDate(def.endDate)
                    .build();

            promotionRepository.save(promotion);
            log.info("Created promotion: {}", def.code);
            created++;
        }

        logCompleted(created, skipped);
    }
    
    private record PromoDef(String code, String title, String description, BigDecimal discountPercent, LocalDateTime startDate, LocalDateTime endDate) {}
}

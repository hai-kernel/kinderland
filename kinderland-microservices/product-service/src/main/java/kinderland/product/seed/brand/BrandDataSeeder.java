package kinderland.product.seed.brand;

import kinderland.common.seed.AbstractDataSeeder;
import kinderland.product.model.entity.Brand;
import kinderland.product.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(20)
@ConditionalOnProperty(name = "app.seed.brand.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class BrandDataSeeder extends AbstractDataSeeder {

    private final BrandRepository brandRepository;

    @Override
    public String getName() {
        return "BrandDataSeeder";
    }

    @Override
    @Transactional
    public void seed() {
        logStart();

        List<BrandDef> brands = List.of(
                new BrandDef("LEGO", "Đan Mạch"),
                new BrandDef("Mattel", "Mỹ"),
                new BrandDef("Fisher-Price", "Mỹ"),
                new BrandDef("Hasbro", "Mỹ"),
                new BrandDef("Hot Wheels", "Mỹ"),
                new BrandDef("Barbie", "Mỹ"),
                new BrandDef("Play-Doh", "Mỹ"),
                new BrandDef("VTech", "Hồng Kông"),
                new BrandDef("Melissa & Doug", "Mỹ"),
                new BrandDef("Hape", "Đức"),
                new BrandDef("Crayola", "Mỹ"),
                new BrandDef("Nerf", "Mỹ")
        );

        int created = 0;
        int skipped = 0;

        for (BrandDef def : brands) {
            if (brandRepository.existsByName(def.name)) {
                log.info("Skipped brand: {}", def.name);
                skipped++;
                continue;
            }

            Brand brand = Brand.builder()
                    .name(def.name)
                    .origin(def.origin)
                    .build();

            brandRepository.save(brand);
            log.info("Created brand: {}", def.name);
            created++;
        }

        logCompleted(created, skipped);
    }
    
    private record BrandDef(String name, String origin) {}
}

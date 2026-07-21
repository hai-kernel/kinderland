package kinderland.product.seed.category;

import kinderland.common.seed.AbstractDataSeeder;
import kinderland.product.model.entity.Category;
import kinderland.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Order(10)
@ConditionalOnProperty(name = "app.seed.category.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class CategoryDataSeeder extends AbstractDataSeeder {

    private final CategoryRepository categoryRepository;

    @Override
    public String getName() {
        return "CategoryDataSeeder";
    }

    @Override
    @Transactional
    public void seed() {
        logStart();

        List<String> categories = List.of(
                "Đồ chơi giáo dục",
                "Đồ chơi lắp ráp",
                "Búp bê và phụ kiện",
                "Xe đồ chơi",
                "Đồ chơi điều khiển từ xa",
                "Đồ chơi vận động",
                "Đồ chơi âm nhạc",
                "Đồ chơi mô phỏng nghề nghiệp",
                "Đồ chơi cho bé sơ sinh",
                "Board game và đồ chơi trí tuệ",
                "Đồ chơi ngoài trời",
                "Đồ dùng học tập sáng tạo"
        );

        int created = 0;
        int skipped = 0;

        for (String name : categories) {
            if (categoryRepository.existsByName(name)) {
                log.info("Skipped category: {}", name);
                skipped++;
                continue;
            }

            Category category = Category.builder()
                    .name(name)
                    .parentId(null)
                    .build();

            categoryRepository.save(category);
            log.info("Created category: {}", name);
            created++;
        }

        logCompleted(created, skipped);
    }
}

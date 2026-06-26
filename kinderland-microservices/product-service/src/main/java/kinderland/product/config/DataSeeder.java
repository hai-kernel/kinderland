package kinderland.product.config;

import kinderland.product.model.entity.Category;
import kinderland.product.model.entity.Product;
import kinderland.product.repository.CategoryRepository;
import kinderland.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds categories and products on startup if they do not already exist.
 *
 * Strategy: checks {@code CategoryRepository.count()} before seeding — if ANY
 * category row exists, the entire seed block is skipped. This keeps the seeder
 * completely idempotent and never overwrites or duplicates existing data.
 *
 * Products are only inserted if their parent category was just created by this
 * seeder run (category id is captured inline).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (categoryRepository.count() > 0) {
            log.info("[DataSeeder] Product data already exists — skipping seed.");
            return;
        }

        log.info("[DataSeeder] Seeding categories and products...");

        // ── Categories ────────────────────────────────────────────────────────
        Category toys       = save(category("Đồ chơi", null));
        Category books      = save(category("Sách thiếu nhi", null));
        Category clothes    = save(category("Quần áo trẻ em", null));
        Category education  = save(category("Học liệu giáo dục", null));

        // Sub-categories
        Category blocks     = save(category("Đồ chơi xếp hình", toys.getId()));
        Category outdoor    = save(category("Đồ chơi ngoài trời", toys.getId()));
        Category storybooks = save(category("Truyện tranh", books.getId()));
        Category workbooks  = save(category("Sách bài tập", books.getId()));

        // ── Products ─────────────────────────────────────────────────────────
        productRepository.saveAll(List.of(

            // Toys — blocks
            Product.builder()
                .name("Bộ xếp hình LEGO Classic 200 mảnh")
                .description("Kích thích tư duy sáng tạo, phù hợp trẻ từ 4 tuổi.")
                .ageRange("4+")
                .gender("Unisex")
                .price(new BigDecimal("349000"))
                .stockQuantity(50)
                .active(true)
                .category(blocks)
                .build(),

            Product.builder()
                .name("Bộ Duplo xây nhà 80 chi tiết")
                .description("Hạt to an toàn cho trẻ nhỏ, phát triển kỹ năng vận động.")
                .ageRange("2-5")
                .gender("Unisex")
                .price(new BigDecimal("279000"))
                .stockQuantity(40)
                .active(true)
                .category(blocks)
                .build(),

            // Toys — outdoor
            Product.builder()
                .name("Xe đạp 3 bánh có đẩy")
                .description("Khung thép sơn tĩnh điện, an toàn cho trẻ 1-3 tuổi.")
                .ageRange("1-3")
                .gender("Unisex")
                .price(new BigDecimal("850000"))
                .stockQuantity(20)
                .active(true)
                .category(outdoor)
                .build(),

            Product.builder()
                .name("Bộ cát biển mini 5 món")
                .description("Nhựa PP an toàn, không BPA. Bao gồm xô, xẻng, khuôn.")
                .ageRange("2+")
                .gender("Unisex")
                .price(new BigDecimal("129000"))
                .stockQuantity(100)
                .active(true)
                .category(outdoor)
                .build(),

            // Books — storybooks
            Product.builder()
                .name("Doraemon tập 1 (bìa màu)")
                .description("Truyện tranh kinh điển, in màu toàn trang, giấy tốt.")
                .ageRange("6+")
                .gender("Unisex")
                .price(new BigDecimal("35000"))
                .stockQuantity(200)
                .active(true)
                .category(storybooks)
                .build(),

            Product.builder()
                .name("Thám tử nhí — Bộ 10 cuốn")
                .description("Rèn luyện tư duy logic, ngôn ngữ sinh động.")
                .ageRange("8+")
                .gender("Unisex")
                .price(new BigDecimal("199000"))
                .stockQuantity(80)
                .active(true)
                .category(storybooks)
                .build(),

            // Books — workbooks
            Product.builder()
                .name("Vở tập viết chữ hoa lớp 1")
                .description("Chuẩn chương trình GDPT 2018, giấy kem mịn.")
                .ageRange("5-7")
                .gender("Unisex")
                .price(new BigDecimal("25000"))
                .stockQuantity(300)
                .active(true)
                .category(workbooks)
                .build(),

            // Clothes
            Product.builder()
                .name("Bộ quần áo cotton hoạt hình size 1-2 tuổi")
                .description("Chất liệu cotton 100%, thoáng mát, dễ giặt.")
                .ageRange("1-2")
                .gender("Unisex")
                .price(new BigDecimal("149000"))
                .stockQuantity(60)
                .active(true)
                .category(clothes)
                .build(),

            Product.builder()
                .name("Áo hoodie thu đông size 3-5 tuổi")
                .description("Cotton nỉ dày, giữ ấm tốt, có mũ liền.")
                .ageRange("3-5")
                .gender("Unisex")
                .price(new BigDecimal("219000"))
                .stockQuantity(45)
                .active(true)
                .category(clothes)
                .build(),

            // Education
            Product.builder()
                .name("Bảng học chữ cái & số điện tử")
                .description("Phát âm chuẩn tiếng Việt, có đèn và nhạc, pin AAA.")
                .ageRange("2-6")
                .gender("Unisex")
                .price(new BigDecimal("189000"))
                .stockQuantity(35)
                .active(true)
                .category(education)
                .build(),

            Product.builder()
                .name("Bộ thẻ học Flashcard tiếng Anh 120 thẻ")
                .description("Hình ảnh rõ nét, phủ bóng chống thấm, từ vựng theo chủ đề.")
                .ageRange("3+")
                .gender("Unisex")
                .price(new BigDecimal("95000"))
                .stockQuantity(150)
                .active(true)
                .category(education)
                .build()
        ));

        log.info("[DataSeeder] Seeded {} categories and 11 products successfully.", categoryRepository.count());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Category category(String name, Long parentId) {
        return Category.builder().name(name).parentId(parentId).build();
    }

    private Category save(Category category) {
        return categoryRepository.save(category);
    }
}

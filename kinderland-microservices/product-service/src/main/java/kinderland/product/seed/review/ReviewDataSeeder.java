package kinderland.product.seed.review;

import kinderland.common.seed.AbstractDataSeeder;
import kinderland.product.model.entity.Review;
import kinderland.product.model.entity.Sku;
import kinderland.product.repository.ReviewRepository;
import kinderland.product.repository.SkuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(70)
@ConditionalOnProperty(name = "app.seed.review.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class ReviewDataSeeder extends AbstractDataSeeder {

    private final ReviewRepository reviewRepository;
    private final SkuRepository skuRepository;

    @Override
    public String getName() {
        return "ReviewDataSeeder";
    }

    @Override
    @Transactional
    public void seed() {
        logStart();

        List<Sku> skus = skuRepository.findAll();
        if (skus.isEmpty()) {
            log.warn("No SKUs found. Cannot seed reviews.");
            return;
        }

        List<String> comments5 = List.of("Sản phẩm đẹp, bé rất thích.", "Chất lượng tốt, đóng gói cẩn thận.", "Giao hàng nhanh, đúng mô tả.", "Bé chơi rất lâu mà không chán.");
        List<String> comments4 = List.of("Sản phẩm tốt nhưng bao bì bị móp nhẹ.", "Màu sắc đẹp nhưng kích thước hơi nhỏ.", "Cũng ổn, phù hợp với giá tiền.");
        List<String> comments3 = List.of("Tạm được, không xuất sắc lắm.", "Bé nhà mình không thích lắm.");
        List<String> comments2 = List.of("Hàng không giống hình lắm.", "Giao thiếu phụ kiện nhỏ.");
        List<String> comments1 = List.of("Chất lượng kém, nhanh hỏng.", "Giao sai mẫu, rất thất vọng.");

        int created = 0;
        int skipped = 0;
        int reviewCount = 0;

        for (int i = 1; i <= 80; i++) {
            Sku sku = skus.get(i % skus.size());
            String email = String.format("customer%02d@kinderland.vn", (i % 35) + 1);

            if (reviewRepository.existsByAccountEmailAndSku_Id(email, sku.getId())) {
                skipped++;
                continue;
            }

            int rating;
            String comment;
            if (i <= 44) { // 55%
                rating = 5;
                comment = comments5.get(i % comments5.size());
            } else if (i <= 64) { // 25%
                rating = 4;
                comment = comments4.get(i % comments4.size());
            } else if (i <= 72) { // 10%
                rating = 3;
                comment = comments3.get(i % comments3.size());
            } else if (i <= 76) { // 5%
                rating = 2;
                comment = comments2.get(i % comments2.size());
            } else { // 5%
                rating = 1;
                comment = comments1.get(i % comments1.size());
            }

            Review review = Review.builder()
                    .accountEmail(email)
                    .sku(sku)
                    .rating(rating)
                    .comment(comment)
                    .build();

            reviewRepository.save(review);
            created++;
            reviewCount++;
            
            if (reviewCount % 10 == 0) {
                log.info("Created {} reviews...", reviewCount);
            }
        }

        logCompleted(created, skipped);
    }
}

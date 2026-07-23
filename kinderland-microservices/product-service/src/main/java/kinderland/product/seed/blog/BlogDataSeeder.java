package kinderland.product.seed.blog;

import kinderland.common.seed.AbstractDataSeeder;
import kinderland.product.model.entity.Blog;
import kinderland.product.model.entity.BlogCategory;
import kinderland.product.repository.BlogCategoryRepository;
import kinderland.product.repository.BlogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(60)
@ConditionalOnProperty(name = "app.seed.blog.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class BlogDataSeeder extends AbstractDataSeeder {

    private final BlogRepository blogRepository;
    private final BlogCategoryRepository blogCategoryRepository;

    @Override
    public String getName() {
        return "BlogDataSeeder";
    }

    @Override
    @Transactional
    public void seed() {
        logStart();

        BlogCategory category = blogCategoryRepository.findAll().stream().findFirst().orElseGet(() ->
                blogCategoryRepository.save(BlogCategory.builder().name("Tin tức đồ chơi").build())
        );

        List<String> titles = List.of(
                "Cách chọn đồ chơi phù hợp theo độ tuổi",
                "10 món đồ chơi phát triển tư duy cho bé",
                "Lợi ích của đồ chơi lắp ráp",
                "Hướng dẫn chơi cùng con tại nhà",
                "Cách vệ sinh đồ chơi an toàn",
                "Đồ chơi STEM là gì?",
                "Chọn đồ chơi cho bé từ 0–12 tháng",
                "Kỹ năng trẻ học được từ board game",
                "Đồ chơi giúp trẻ phát triển ngôn ngữ",
                "Gợi ý quà sinh nhật cho bé trai",
                "Gợi ý quà sinh nhật cho bé gái",
                "Cách hạn chế thời gian sử dụng thiết bị điện tử",
                "Trò chơi vận động trong nhà",
                "Lợi ích của hoạt động tô màu",
                "Cách sắp xếp góc chơi cho trẻ",
                "Những lưu ý khi mua đồ chơi điều khiển từ xa",
                "Đồ chơi bằng gỗ có tốt không?",
                "Cách nhận biết đồ chơi an toàn",
                "Hoạt động gia đình cuối tuần",
                "Top sản phẩm nổi bật tháng này"
        );

        int created = 0;
        int skipped = 0;

        for (String title : titles) {
            if (blogRepository.existsByTitle(title)) {
                skipped++;
                continue;
            }

            Blog blog = Blog.builder()
                    .title(title)
                    .content("<p>Nội dung chi tiết cho bài viết: " + title + "</p>")
                    .authorEmail("admin@kinderland.vn")
                    .category(category)
                    .status(true) // published
                    .timeRead(5)
                    .build();

            blogRepository.save(blog);
            log.info("Created blog: {}", title);
            created++;
        }

        logCompleted(created, skipped);
    }
}

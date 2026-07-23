package kinderland.product.seed.product;

import kinderland.common.seed.AbstractDataSeeder;
import kinderland.product.model.entity.EntityType;
import kinderland.product.model.entity.Image;
import kinderland.product.model.entity.Product;
import kinderland.product.repository.ImageRepository;
import kinderland.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Order(40)
@ConditionalOnProperty(name = "app.seed.product-image.enabled", havingValue = "true")
@RequiredArgsConstructor
public class ProductImageDataSeeder extends AbstractDataSeeder {

    private final ProductRepository productRepository;
    private final ImageRepository imageRepository;

    @Override
    public String getName() {
        return "ProductImageDataSeeder";
    }

    @Override
    @Transactional
    public void seed() {
        logStart();

        List<Product> products = productRepository.findAll();
        int created = 0;
        int skipped = 0;

        for (Product product : products) {
            if (imageRepository.existsByEntityTypeAndEntityId(EntityType.PRODUCT, product.getId())) {
                skipped++;
                continue;
            }

            // ĐÚNG 1 ảnh bìa mỗi sản phẩm. Comment cũ ghi "3 images per product" là tàn dư
            // của phiên bản trước — chính 3 dòng ảnh/sản phẩm đó khiến ProductService lưu
            // ảnh vào một dòng nhưng hiển thị dòng khác.
            String imageUrl = getProductImage(product.getName());

            Image image = Image.builder()
                    .imageUrl(imageUrl)
                    .fileName(product.getName() + ".jpg")
                    .entityType(EntityType.PRODUCT)
                    .entityId(product.getId())
                    .build();

            imageRepository.save(image);
            created++;
        }

        logCompleted(created, skipped);
    }private String getProductImage(String productName) {

        if (productName.contains("LEGO")) {
            return "https://images.unsplash.com/photo-1587654780291-39c9404d746b";
        }

        if (productName.contains("Hot Wheels")) {
            return "https://images.unsplash.com/photo-1558618666-fcd25c85cd64";
        }

        if (productName.contains("Barbie")) {
            return "https://images.unsplash.com/photo-1594787318286-3d835c1d207f";
        }

        if (productName.contains("Play-Doh")) {
            return "https://images.unsplash.com/photo-1596461404969-9ae70f2830c1";
        }

        return "https://images.unsplash.com/photo-1566576912320-d58ddd7a6088";
    }
}

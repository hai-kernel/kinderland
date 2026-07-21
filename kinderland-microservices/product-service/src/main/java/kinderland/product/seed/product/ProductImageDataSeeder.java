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
@ConditionalOnProperty(name = "app.seed.product-image.enabled", havingValue = "true", matchIfMissing = true)
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

            // Create 3 images per product
            for (int i = 1; i <= 3; i++) {
                String placeholderUrl = "https://placehold.co/600x600?text=" + product.getName().replace(" ", "+") + "+" + i;
                
                Image image = Image.builder()
                        .imageUrl(placeholderUrl)
                        .fileName("placeholder-" + product.getName() + "-" + i + ".jpg")
                        .entityType(EntityType.PRODUCT)
                        .entityId(product.getId())
                        .build();
                
                imageRepository.save(image);
                created++;
            }
        }

        logCompleted(created, skipped);
    }
}

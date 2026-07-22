package kinderland.product.seed.product;

import kinderland.common.seed.AbstractDataSeeder;
import kinderland.product.model.entity.*;
import kinderland.product.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(30)
@ConditionalOnProperty(name = "app.seed.product.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class ProductDataSeeder extends AbstractDataSeeder {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final SkuRepository skuRepository;
    private final StoreRepository storeRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public String getName() {
        return "ProductDataSeeder";
    }

    @Override
    @Transactional
    public void seed() {
        logStart();

        Store store = storeRepository.findAll().stream().findFirst().orElseGet(() ->
                storeRepository.save(Store.builder()
                        .name("Kinderland Default Store")
                        .code("KL-DEF")
                        .address("123 Kinderland")
                        .active(true)
                        .build())
        );

        List<ProductDef> products = new ArrayList<>();
        products.add(new ProductDef("Bộ xếp hình LEGO Classic 500 chi tiết", "Đồ chơi lắp ráp", "LEGO", new BigDecimal("599000"), 100));
        products.add(new ProductDef("Xe Hot Wheels mô hình thể thao", "Xe đồ chơi", "Hot Wheels", new BigDecimal("99000"), 150));
        products.add(new ProductDef("Búp bê Barbie Dreamtopia", "Búp bê và phụ kiện", "Barbie", new BigDecimal("249000"), 80));
        products.add(new ProductDef("Bộ đất nặn Play-Doh 12 màu", "Đồ dùng học tập sáng tạo", "Play-Doh", new BigDecimal("199000"), 120));
        products.add(new ProductDef("Bảng chữ cái điện tử VTech", "Đồ chơi giáo dục", "VTech", new BigDecimal("399000"), 50));
        products.add(new ProductDef("Bộ đường ray tàu hỏa bằng gỗ Hape", "Đồ chơi lắp ráp", "Hape", new BigDecimal("799000"), 30));
        products.add(new ProductDef("Bộ ghép hình động vật 100 mảnh", "Đồ chơi giáo dục", "Melissa & Doug", new BigDecimal("149000"), 200));
        products.add(new ProductDef("Đàn piano mini cho bé", "Đồ chơi âm nhạc", "Fisher-Price", new BigDecimal("599000"), 45));
        products.add(new ProductDef("Bộ bác sĩ đồ chơi", "Đồ chơi mô phỏng nghề nghiệp", "Mattel", new BigDecimal("249000"), 90));
        products.add(new ProductDef("Xe điều khiển từ xa địa hình", "Đồ chơi điều khiển từ xa", "Hasbro", new BigDecimal("999000"), 25));
        products.add(new ProductDef("Thảm chơi cho bé sơ sinh", "Đồ chơi cho bé sơ sinh", "Fisher-Price", new BigDecimal("799000"), 40));
        products.add(new ProductDef("Bộ cờ cá ngựa gia đình", "Board game và đồ chơi trí tuệ", "Crayola", new BigDecimal("149000"), 110));
        products.add(new ProductDef("Bộ đồ chơi nhà bếp", "Đồ chơi mô phỏng nghề nghiệp", "Melissa & Doug", new BigDecimal("1299000"), 15));
        products.add(new ProductDef("Bộ khối gỗ nhiều màu", "Đồ chơi lắp ráp", "Hape", new BigDecimal("399000"), 60));
        products.add(new ProductDef("Robot lập trình cho trẻ em", "Đồ chơi giáo dục", "VTech", new BigDecimal("1999000"), 10));
        products.add(new ProductDef("Bộ tô màu Crayola 64 màu", "Đồ dùng học tập sáng tạo", "Crayola", new BigDecimal("199000"), 180));
        products.add(new ProductDef("Súng đồ chơi Nerf an toàn cho trẻ em", "Đồ chơi vận động", "Nerf", new BigDecimal("399000"), 70));
        products.add(new ProductDef("Bộ siêu thị mini cho bé", "Đồ chơi mô phỏng nghề nghiệp", "Mattel", new BigDecimal("999000"), 20));
        products.add(new ProductDef("Xe cứu hỏa mô hình có âm thanh", "Xe đồ chơi", "Hasbro", new BigDecimal("249000"), 85));
        products.add(new ProductDef("Bộ ghép hình bản đồ Việt Nam", "Đồ chơi giáo dục", "Hape", new BigDecimal("149000"), 130));



        int created = 0;
        int skipped = 0;

        for (int i = 0; i < products.size(); i++) {
            ProductDef def = products.get(i);
            
            if (productRepository.existsByName(def.name)) {
                log.info("Skipped product: {}", def.name);
                skipped++;
                continue;
            }

            Category category = categoryRepository.findByName(def.categoryName)
                    .orElseThrow(() -> new IllegalStateException("Missing category seed: " + def.categoryName));
            Brand brand = brandRepository.findByName(def.brandName)
                    .orElseThrow(() -> new IllegalStateException("Missing brand seed: " + def.brandName));

            Product product = Product.builder()
                    .name(def.name)
                    .description("Sản phẩm tuyệt vời cho bé: " + def.name)
                    .ageRange("3+")
                    .gender("Unisex")
                    .price(def.price)
                    .stockQuantity(def.stockQuantity)
                    .active(true)
                    .category(category)
                    .brand(brand)
                    .build();

            productRepository.save(product);

            String skuCode = String.format("SKU-%03d", i + 1);
            Sku sku = Sku.builder()
                    .product(product)
                    .skuCode(skuCode)
                    .size("Standard")
                    .type("default")
                    .price(def.price)
                    .build();
            skuRepository.save(sku);

            Inventory inventory = Inventory.builder()
                    .sku(sku)
                    .store(store)
                    .quantity(def.stockQuantity)
                    .build();
            inventoryRepository.save(inventory);

            log.info("Created product: {}", def.name);
            created++;
        }

        logCompleted(created, skipped);
    }

    private record ProductDef(String name, String categoryName, String brandName, BigDecimal price, int stockQuantity) {}
}

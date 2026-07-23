package kinderland.product.repository;

import kinderland.product.model.entity.Product;
import kinderland.product.model.entity.Sku;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Kiểm chứng hành vi XOÁ MỀM ở tầng dữ liệu — nơi bug gốc nằm.
 *
 * Trọng tâm: sản phẩm bị "xoá" PHẢI còn nguyên trong bảng products (để SKU tham chiếu
 * không vi phạm khoá ngoại), nhưng biến mất khỏi mọi truy vấn hiển thị.
 *
 * Dùng @DataJpaTest (chỉ nạp lát JPA + H2 in-memory) thay vì @SpringBootTest: không cần
 * PostgreSQL, không cần Config Server / Eureka / Kafka.
 */
@DataJpaTest
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@DisplayName("Xoá mềm sản phẩm")
class ProductSoftDeleteTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SkuRepository skuRepository;

    private Product product;

    @BeforeEach
    void setUp() {
        product = productRepository.save(Product.builder()
                .name("Bộ xếp hình LEGO Classic")
                .description("Đồ chơi lắp ráp")
                .price(new BigDecimal("599000"))
                .stockQuantity(100)
                .active(true)
                .build());

        // SKU trỏ tới product — chính ràng buộc khiến xoá cứng ném
        // "violates foreign key constraint on table sku".
        skuRepository.save(Sku.builder()
                .product(product)
                .skuCode("SKU-001")
                .size("Standard")
                .type("default")
                .price(new BigDecimal("599000"))
                .build());
    }

    @Test
    @DisplayName("Sản phẩm mới tạo có deleted = false và deletedAt = null")
    void newProductIsNotDeleted() {
        assertThat(product.isDeleted()).isFalse();
        assertThat(product.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("Xoá mềm: dòng VẪN CÒN trong database, SKU không bị ảnh hưởng")
    void softDeleteKeepsRowAndSku() {
        product.setDeleted(true);
        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);

        Optional<Product> stillThere = productRepository.findById(product.getId());
        assertThat(stillThere).isPresent();
        assertThat(stillThere.get().isDeleted()).isTrue();
        assertThat(stillThere.get().getDeletedAt()).isNotNull();

        // SKU vẫn trỏ được về product -> không có FK nào bị phá.
        assertThat(skuRepository.findAll()).hasSize(1);
        assertThat(skuRepository.findAll().get(0).getProduct().getId()).isEqualTo(product.getId());
    }

    @Test
    @DisplayName("Xoá mềm KHÔNG đụng tới 'active' (trạng thái kinh doanh)")
    void softDeleteDoesNotTouchActive() {
        product.setActive(false);          // admin đã chủ động ngừng bán từ trước
        productRepository.save(product);

        product.setDeleted(true);
        productRepository.save(product);
        product.setDeleted(false);         // khôi phục
        productRepository.save(product);

        // Khôi phục KHÔNG được tự bật bán lại.
        assertThat(productRepository.findById(product.getId()).orElseThrow().isActive()).isFalse();
    }

    @Test
    @DisplayName("Sau khi xoá mềm: biến mất khỏi danh sách thường, xuất hiện trong thùng rác")
    void deletedProductHiddenFromListAndShownInTrash() {
        assertThat(productRepository.findByDeletedFalse()).hasSize(1);
        assertThat(productRepository.findByDeletedTrue()).isEmpty();

        product.setDeleted(true);
        productRepository.save(product);

        assertThat(productRepository.findByDeletedFalse()).isEmpty();
        assertThat(productRepository.findByDeletedTrue())
                .hasSize(1)
                .first()
                .extracting(Product::getId)
                .isEqualTo(product.getId());
    }

    @Test
    @DisplayName("browse() bỏ qua sản phẩm đã xoá mềm")
    void browseExcludesDeleted() {
        List<Product> before = productRepository.browse(null, null, null, null, null);
        assertThat(before).hasSize(1);

        product.setDeleted(true);
        productRepository.save(product);

        assertThat(productRepository.browse(null, null, null, null, null)).isEmpty();
    }

    @Test
    @DisplayName("Khôi phục: sản phẩm hiện lại ở danh sách thường và rời thùng rác")
    void restoreMakesProductVisibleAgain() {
        product.setDeleted(true);
        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);
        assertThat(productRepository.findByDeletedFalse()).isEmpty();

        product.setDeleted(false);
        product.setDeletedAt(null);
        productRepository.save(product);

        assertThat(productRepository.findByDeletedFalse()).hasSize(1);
        assertThat(productRepository.findByDeletedTrue()).isEmpty();
        assertThat(productRepository.findById(product.getId()).orElseThrow().getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("findByIdAndDeletedFalse: không trả về hàng đã xoá")
    void findByIdSkipsDeleted() {
        assertThat(productRepository.findByIdAndDeletedFalse(product.getId())).isPresent();

        product.setDeleted(true);
        productRepository.save(product);

        assertThat(productRepository.findByIdAndDeletedFalse(product.getId())).isEmpty();
        // Nhưng findById thường vẫn thấy — cần cho chức năng khôi phục.
        assertThat(productRepository.findById(product.getId())).isPresent();
    }
}

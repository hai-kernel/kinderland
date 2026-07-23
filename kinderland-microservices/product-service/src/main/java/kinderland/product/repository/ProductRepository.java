package kinderland.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import kinderland.product.model.entity.Product;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Long categoryId);
    boolean existsByName(String name);

    /** Duyệt/tìm sản phẩm với bộ lọc tuỳ chọn (param null = bỏ qua). Lọc giá theo Product.price (fallback). */
    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:brandId IS NULL OR p.brand.id = :brandId) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    List<Product> browse(@Param("keyword") String keyword,
                         @Param("categoryId") Long categoryId,
                         @Param("brandId") Long brandId,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice);

    /** Còn sản phẩm nào thuộc danh mục / thương hiệu này không (chặn xoá khi đang bị tham chiếu). */
    boolean existsByCategoryId(Long categoryId);
    boolean existsByBrandId(Long brandId);

    /** Các sản phẩm đang gán promotion (dùng cho promotion detail & khi xoá promotion). */
    List<Product> findByPromotion_PromotionId(Long promotionId);
}

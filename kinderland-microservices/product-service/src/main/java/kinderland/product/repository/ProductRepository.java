package kinderland.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import kinderland.product.model.entity.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryIdAndDeletedFalse(Long categoryId);
    boolean existsByName(String name);

    /** Danh sách thường: LOẠI sản phẩm đã xoá mềm. Dùng cho mọi API hiển thị. */
    List<Product> findByDeletedFalse();

    /** Thùng rác: CHỈ sản phẩm đã xoá mềm. Chỉ ADMIN được gọi. */
    List<Product> findByDeletedTrue();

    /** Tìm theo id nhưng bỏ qua bản ghi đã xoá mềm (dùng cho API công khai). */
    Optional<Product> findByIdAndDeletedFalse(Long id);

    /** Duyệt/tìm sản phẩm với bộ lọc tuỳ chọn (param null = bỏ qua). Lọc giá theo Product.price (fallback). */
    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.active = true AND " +
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

    /**
     * Còn sản phẩm nào thuộc danh mục / thương hiệu này không (chặn xoá khi đang bị tham chiếu).
     * Bỏ qua sản phẩm đã xoá mềm: hàng trong thùng rác không được giữ danh mục "sống" mãi.
     */
    boolean existsByCategoryIdAndDeletedFalse(Long categoryId);
    boolean existsByBrandIdAndDeletedFalse(Long brandId);

    /**
     * Có BẤT KỲ product nào trỏ tới danh mục/thương hiệu này không — KỂ CẢ hàng đã xoá mềm.
     *
     * Dùng để chặn XOÁ CỨNG category/brand. Khoá ngoại products.category_id / brand_id
     * KHÔNG quan tâm cờ 'deleted' của tầng ứng dụng: sản phẩm nằm trong thùng rác vẫn giữ
     * FK, nên xoá brand lúc đó vẫn ném "violates foreign key constraint".
     * Bản ...AndDeletedFalse ở trên CHỈ dùng cho nghiệp vụ hiển thị, KHÔNG dùng làm rào xoá.
     */
    boolean existsByCategoryId(Long categoryId);
    boolean existsByBrandId(Long brandId);

    /** Các sản phẩm đang gán promotion (dùng cho promotion detail & khi xoá promotion). */
    List<Product> findByPromotion_PromotionIdAndDeletedFalse(Long promotionId);
}

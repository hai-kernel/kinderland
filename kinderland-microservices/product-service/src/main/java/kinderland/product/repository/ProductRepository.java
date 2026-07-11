package kinderland.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import kinderland.product.model.entity.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Long categoryId);

    /** Còn sản phẩm nào thuộc danh mục / thương hiệu này không (chặn xoá khi đang bị tham chiếu). */
    boolean existsByCategoryId(Long categoryId);
    boolean existsByBrandId(Long brandId);

    /** Các sản phẩm đang gán promotion (dùng cho promotion detail & khi xoá promotion). */
    List<Product> findByPromotion_PromotionId(Long promotionId);
}

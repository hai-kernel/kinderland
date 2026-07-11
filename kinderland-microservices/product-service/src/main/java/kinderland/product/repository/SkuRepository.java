package kinderland.product.repository;

import kinderland.product.model.entity.Sku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface SkuRepository extends JpaRepository<Sku, Long> {

    List<Sku> findByProductId(Long productId);

    boolean existsByProductId(Long productId);

    /** Giá nhỏ nhất trong các SKU của 1 product (dùng cho Product.minPrice). null nếu chưa có SKU. */
    @Query("select min(s.price) from Sku s where s.product.id = :productId")
    BigDecimal findMinPriceByProductId(@Param("productId") Long productId);
}

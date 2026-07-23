package kinderland.product.repository;

import kinderland.product.model.entity.Sku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface SkuRepository extends JpaRepository<Sku, Long> {

    List<Sku> findByProductId(Long productId);

    /**
     * SKU của các sản phẩm CHƯA bị xoá mềm — dùng cho mọi danh sách hiển thị cho khách.
     *
     * skuRepository.findAll() trả về cả SKU của sản phẩm đã nằm trong thùng rác, nên
     * carousel "Có thể bạn cũng thích" vẫn hiện sản phẩm admin đã xoá.
     * KHÔNG áp bộ lọc này cho getById/getInternal: đơn hàng cũ phải tra cứu được SKU
     * của sản phẩm đã xoá, nếu không lịch sử mua hàng sẽ vỡ.
     */
    @Query("select s from Sku s where s.product.deleted = false")
    List<Sku> findAllByProductNotDeleted();

    boolean existsByProductId(Long productId);

    /** Giá nhỏ nhất trong các SKU của 1 product (dùng cho Product.minPrice). null nếu chưa có SKU. */
    @Query("select min(s.price) from Sku s where s.product.id = :productId")
    BigDecimal findMinPriceByProductId(@Param("productId") Long productId);
}

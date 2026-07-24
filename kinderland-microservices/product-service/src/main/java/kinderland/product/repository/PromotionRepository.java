package kinderland.product.repository;

import kinderland.product.model.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    boolean existsByCode(String code);

    Optional<Promotion> findByCode(String code);

    /**
     * Ghi nhận 1 lượt dùng mã, ATOMIC ở tầng DB: điều kiện usage_limit nằm ngay trong UPDATE
     * nên hai đơn thanh toán đồng thời không thể cùng vượt hạn mức (read-then-write ở tầng
     * Java sẽ bị race). Trả về số dòng cập nhật: 0 = đã hết lượt.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Promotion p SET p.usedCount = p.usedCount + 1 " +
            "WHERE p.promotionId = :id AND (p.usageLimit IS NULL OR p.usedCount < p.usageLimit)")
    int incrementUsedCount(@Param("id") Long id);

    @Query("SELECT p FROM Promotion p WHERE " +
            ":keyword IS NULL OR :keyword = '' OR " +
            "LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Promotion> searchByMultipleFields(@Param("keyword") String keyword, Pageable pageable);
}

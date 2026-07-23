package kinderland.product.repository;

import kinderland.product.model.entity.EntityType;
import kinderland.product.model.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    /** Lấy tất cả ảnh của một đối tượng (vd toàn bộ ảnh của 1 product). */
    List<Image> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);

    /**
     * Ảnh của một entity, SẮP XẾP theo id tăng dần.
     *
     * BẮT BUỘC có ORDER BY: một product có thể có nhiều dòng ảnh (dữ liệu seed cũ tạo 3
     * dòng/sản phẩm). Truy vấn không ORDER BY trả về theo thứ tự vật lý, mà PostgreSQL
     * ghi bản mới của dòng vừa UPDATE xuống CUỐI bảng — nên sau khi đổi ảnh bìa, dòng
     * mới nhất lại nằm cuối và findFirst() nhặt đúng dòng ảnh cũ. Đó là lý do "cập nhật
     * thành công" nhưng ảnh hiển thị không đổi.
     */
    List<Image> findByEntityTypeAndEntityIdOrderByIdAsc(EntityType entityType, Long entityId);
    boolean existsByEntityTypeAndEntityId(EntityType entityType, Long entityId);
}

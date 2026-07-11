package kinderland.product.repository;

import kinderland.product.model.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    /** Bài đã xuất bản, chưa xoá (danh sách công khai). */
    List<Blog> findByStatusTrueAndDeletedFalseOrderByPublishedAtDesc();

    /** Tất cả bài chưa xoá (cho admin). */
    List<Blog> findByDeletedFalseOrderByCreatedAtDesc();

    Optional<Blog> findByIdAndDeletedFalse(Long id);

    /** Còn bài viết nào thuộc danh mục blog này không (kể cả bài đã xoá mềm vẫn giữ FK). */
    boolean existsByCategoryId(Long categoryId);
}

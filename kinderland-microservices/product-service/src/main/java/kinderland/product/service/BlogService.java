package kinderland.product.service;

import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.common.security.GatewayAuthContext;
import kinderland.product.model.dto.request.BlogRequest;
import kinderland.product.model.dto.response.BlogResponse;
import kinderland.product.model.entity.Blog;
import kinderland.product.model.entity.BlogCategory;
import kinderland.product.repository.BlogCategoryRepository;
import kinderland.product.repository.BlogRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BlogService {

    BlogRepository blogRepository;
    BlogCategoryRepository blogCategoryRepository;

    /** Danh sách bài đã xuất bản (công khai). */
    public List<BlogResponse> getPublished() {
        return blogRepository.findByStatusTrueAndDeletedFalseOrderByPublishedAtDesc()
                .stream().map(this::toResponse).toList();
    }

    /** Tất cả bài chưa xoá (admin). */
    public List<BlogResponse> getAllForAdmin() {
        return blogRepository.findByDeletedFalseOrderByCreatedAtDesc()
                .stream().map(this::toResponse).toList();
    }

    public BlogResponse getById(Long id) {
        Blog blog = findEntity(id);
        // Bài chưa xuất bản (nháp) chỉ admin mới xem được; khách coi như không tồn tại.
        if (!blog.isStatus() && !"ROLE_ADMIN".equals(GatewayAuthContext.getCurrentRole())) {
            throw new AppException(ErrorCode.BLOG_NOT_FOUND);
        }
        return toResponse(blog);
    }

    @Transactional
    public BlogResponse create(String authorEmail, BlogRequest request) {
        Blog blog = Blog.builder()
                .authorEmail(authorEmail)
                .title(request.getTitle())
                .content(request.getContent())
                .category(resolveCategory(request.getCategoryId()))
                .status(request.isStatus())
                .timeRead(request.getTimeRead())
                .build();
        return toResponse(blogRepository.save(blog));
    }

    @Transactional
    public BlogResponse update(Long id, BlogRequest request) {
        Blog blog = findEntity(id);
        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        blog.setCategory(resolveCategory(request.getCategoryId()));
        blog.setStatus(request.isStatus());
        blog.setTimeRead(request.getTimeRead());
        return toResponse(blogRepository.save(blog));
    }

    /** Bật/tắt xuất bản. */
    @Transactional
    public BlogResponse toggleStatus(Long id) {
        Blog blog = findEntity(id);
        blog.setStatus(!blog.isStatus());
        return toResponse(blogRepository.save(blog));
    }

    /** Xoá mềm (đặt cờ deleted). */
    @Transactional
    public void delete(Long id) {
        Blog blog = findEntity(id);
        blog.setDeleted(true);
        blogRepository.save(blog);
    }

    private BlogCategory resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return blogCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.BLOG_CATEGORY_NOT_FOUND));
    }

    private Blog findEntity(Long id) {
        return blogRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_FOUND));
    }

    private BlogResponse toResponse(Blog b) {
        return BlogResponse.builder()
                .id(b.getId())
                .authorEmail(b.getAuthorEmail())
                .title(b.getTitle())
                .content(b.getContent())
                .categoryId(b.getCategory() == null ? null : b.getCategory().getId())
                .categoryName(b.getCategory() == null ? null : b.getCategory().getName())
                .status(b.isStatus())
                .timeRead(b.getTimeRead())
                .publishedAt(b.getPublishedAt())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}

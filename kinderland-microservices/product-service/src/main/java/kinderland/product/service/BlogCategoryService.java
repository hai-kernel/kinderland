package kinderland.product.service;

import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.product.mapper.BlogCategoryMapper;
import kinderland.product.model.dto.request.BlogCategoryRequest;
import kinderland.product.model.dto.response.BlogCategoryResponse;
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
public class BlogCategoryService {

    BlogCategoryRepository repository;
    BlogRepository blogRepository;
    BlogCategoryMapper mapper;

    @Transactional
    public BlogCategoryResponse create(BlogCategoryRequest request) {
        if (repository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.BLOG_CATEGORY_ALREADY_EXISTS);
        }
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Transactional
    public BlogCategoryResponse update(Long id, BlogCategoryRequest request) {
        BlogCategory category = findEntity(id);
        mapper.updateEntity(request, category);
        return mapper.toResponse(repository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        BlogCategory category = findEntity(id);
        if (blogRepository.existsByCategoryId(id)) {
            throw new AppException(ErrorCode.BLOG_CATEGORY_IN_USE);
        }
        repository.delete(category);
    }

    public BlogCategoryResponse getById(Long id) {
        return mapper.toResponse(findEntity(id));
    }

    public List<BlogCategoryResponse> getAll() {
        return mapper.toResponseList(repository.findAll());
    }

    private BlogCategory findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BLOG_CATEGORY_NOT_FOUND));
    }
}

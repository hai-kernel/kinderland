package kinderland.product.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.product.mapper.ProductMapper;
import kinderland.product.model.dto.internal.ProductInternalResponse;
import kinderland.product.model.dto.request.ProductRequest;
import kinderland.product.model.dto.response.ProductResponse;
import kinderland.product.model.entity.Category;
import kinderland.product.model.entity.Product;
import kinderland.product.repository.CategoryRepository;
import kinderland.product.repository.ProductRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService {

    ProductRepository productRepository;
    CategoryRepository categoryRepository;
    ProductMapper productMapper;

    // ---------- CRUD cho Frontend ----------
    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = productMapper.toEntity(request);
        product.setActive(true);
        product.setCategory(resolveCategory(request.getCategoryId()));
        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findEntity(id);
        productMapper.updateEntity(request, product);
        product.setCategory(resolveCategory(request.getCategoryId()));
        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        productRepository.delete(findEntity(id));
    }

    public ProductResponse getById(Long id) {
        return productMapper.toResponse(findEntity(id));
    }

    public List<ProductResponse> getAll() {
        return productMapper.toResponseList(productRepository.findAll());
    }

    // ---------- Internal API (Order Service gọi qua Feign) ----------
    public ProductInternalResponse getInternal(Long id) {
        return productMapper.toInternal(findEntity(id));
    }

    /** Trừ kho khi Order Service tạo đơn thành công. Ném lỗi nếu không đủ tồn. */
    @Transactional
    public ProductInternalResponse decrementStock(Long id, int quantity) {
        Product p = findEntity(id);
        if (p.getStockQuantity() < quantity) {
            throw new AppException(ErrorCode.OUT_OF_STOCK);
        }
        p.setStockQuantity(p.getStockQuantity() - quantity);
        productRepository.save(p);
        return productMapper.toInternal(p);
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private Product findEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}

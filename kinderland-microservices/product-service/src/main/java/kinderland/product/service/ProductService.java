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
import kinderland.product.model.entity.Brand;
import kinderland.product.model.entity.Category;
import kinderland.product.model.entity.EntityType;
import kinderland.product.model.entity.Image;
import kinderland.product.model.entity.Product;
import kinderland.product.repository.BrandRepository;
import kinderland.product.repository.CategoryRepository;
import kinderland.product.repository.ImageRepository;
import kinderland.product.repository.ProductRepository;
import kinderland.product.repository.SkuRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService {

    ProductRepository productRepository;
    CategoryRepository categoryRepository;
    BrandRepository brandRepository;
    ImageRepository imageRepository;
    SkuRepository skuRepository;
    S3Service s3Service;
    ProductMapper productMapper;

    // ---------- CRUD cho Frontend ----------
    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = productMapper.toEntity(request);
        product.setActive(true);
        product.setCategory(resolveCategory(request.getCategoryId()));
        product.setBrand(resolveBrand(request.getBrandId()));
        applyDefaults(product);
        Product saved = productRepository.save(product);
        upsertProductImage(saved.getId(), request.getImageUrl());
        return toResponse(saved);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findEntity(id);
        productMapper.updateEntity(request, product);
        product.setCategory(resolveCategory(request.getCategoryId()));
        product.setBrand(resolveBrand(request.getBrandId()));
        applyDefaults(product);
        Product saved = productRepository.save(product);
        upsertProductImage(saved.getId(), request.getImageUrl());
        return toResponse(saved);
    }

    /**
     * XOÁ MỀM: đặt deleted = true, KHÔNG xoá dòng khỏi database.
     *
     * Xoá cứng bất khả thi — product bị sku -> inventory/reviews/transfer_orders tham
     * chiếu, và bởi order_items ở order-service (database KHÁC, không có FK để chặn).
     * Xoá cứng ném "violates foreign key constraint on table sku", hoặc tệ hơn là làm
     * đơn hàng cũ mất thông tin sản phẩm đã bán.
     *
     * KHÔNG đụng tới 'active': đó là trạng thái kinh doanh do admin đặt, khôi phục phải
     * trả sản phẩm về đúng trạng thái bán/ngừng bán trước khi xoá.
     */
    @Transactional
    public void delete(Long id) {
        Product product = findEntity(id);
        if (product.isDeleted()) {
            return; // đã ở thùng rác -> idempotent, gọi lại không lỗi
        }
        product.setDeleted(true);
        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    /** Khôi phục sản phẩm từ thùng rác. Giữ nguyên 'active' như trước khi xoá. */
    @Transactional
    public ProductResponse restore(Long id) {
        Product product = findEntity(id);
        product.setDeleted(false);
        product.setDeletedAt(null);
        return toResponse(productRepository.save(product));
    }

    /** Thùng rác: chỉ sản phẩm đã xoá mềm. Dành cho ADMIN. */
    public List<ProductResponse> getTrash() {
        return productRepository.findByDeletedTrue().stream().map(this::toResponse).toList();
    }

    public ProductResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    /** Danh sách sản phẩm — luôn LOẠI hàng đã xoá mềm. */
    public List<ProductResponse> getAll() {
        return productRepository.findByDeletedFalse().stream().map(this::toResponse).toList();
    }

    /** Duyệt sản phẩm có lọc (khớp FE productApi.browse). */
    public List<ProductResponse> browse(String keyword, Long categoryId, Long brandId,
                                        BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.browse(
                        keyword == null || keyword.isBlank() ? null : keyword,
                        categoryId, brandId, minPrice, maxPrice)
                .stream().map(this::toResponse).toList();
    }

    /** Tìm sản phẩm theo từ khoá (khớp FE productApi.search). */
    public List<ProductResponse> search(String keyword) {
        return browse(keyword, null, null, null, null);
    }

    /** Map entity -> response + đính presigned URL của ảnh bìa (ảnh đầu tiên của product). */
    private ProductResponse toResponse(Product product) {
        ProductResponse response = productMapper.toResponse(product);
        // minPrice = giá SKU nhỏ nhất nếu product đã có SKU; chưa có thì giữ price của product (fallback).
        BigDecimal skuMin = skuRepository.findMinPriceByProductId(product.getId());
        if (skuMin != null) {
            response.setMinPrice(skuMin);
        }
        imageRepository.findByEntityTypeAndEntityId(EntityType.PRODUCT, product.getId())
                .stream().findFirst()
                .ifPresent(img -> response.setImageUrl(s3Service.resolveImageUrl(img.getImageUrl())));
        return response;
    }

    /** Lưu/cập nhật ảnh bìa của product (FE truyền S3 key qua request.imageUrl). */
    private void upsertProductImage(Long productId, String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        List<Image> existing = imageRepository.findByEntityTypeAndEntityId(EntityType.PRODUCT, productId);
        if (!existing.isEmpty()) {
            Image img = existing.get(0);
            img.setImageUrl(key);
            imageRepository.save(img);
        } else {
            imageRepository.save(Image.builder()
                    .entityType(EntityType.PRODUCT)
                    .entityId(productId)
                    .imageUrl(key)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }

    /** FE (mô hình SKU) không gửi price/stock -> mặc định 0 để không vi phạm cột NOT NULL. */
    private void applyDefaults(Product product) {
        if (product.getPrice() == null) {
            product.setPrice(BigDecimal.ZERO);
        }
        if (product.getStockQuantity() == null) {
            product.setStockQuantity(0);
        }
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

    /** Cộng kho lại khi đơn bị huỷ (bù cho lần trừ kho lúc tạo đơn). */
    @Transactional
    public void restockStock(Long id, int quantity) {
        Product p = findEntity(id);
        p.setStockQuantity(p.getStockQuantity() + quantity);
        productRepository.save(p);
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private Brand resolveBrand(Long brandId) {
        if (brandId == null) {
            return null;
        }
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
    }

    private Product findEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}

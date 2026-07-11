package kinderland.product.service;

import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.product.mapper.PromotionMapper;
import kinderland.product.model.dto.request.PromotionRequest;
import kinderland.product.model.dto.response.PromotionProductResponse;
import kinderland.product.model.dto.response.PromotionResponse;
import kinderland.product.model.entity.EntityType;
import kinderland.product.model.entity.Product;
import kinderland.product.model.entity.Promotion;
import kinderland.product.repository.ImageRepository;
import kinderland.product.repository.ProductRepository;
import kinderland.product.repository.PromotionRepository;
import kinderland.product.repository.SkuRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PromotionService {

    PromotionRepository promotionRepository;
    ProductRepository productRepository;
    SkuRepository skuRepository;
    ImageRepository imageRepository;
    S3Service s3Service;
    PromotionMapper promotionMapper;

    @Transactional
    public PromotionResponse create(PromotionRequest request) {
        if (request.getCode() != null && promotionRepository.existsByCode(request.getCode().toUpperCase())) {
            throw new AppException(ErrorCode.PROMOTION_CODE_EXISTS);
        }
        Promotion promotion = promotionMapper.toEntity(request);
        promotion.setCode(normalizeCode(request.getCode()));
        return promotionMapper.toResponse(promotionRepository.save(promotion));
    }

    @Transactional
    public PromotionResponse update(Long id, PromotionRequest request) {
        Promotion promotion = findEntity(id);
        if (request.getCode() != null
                && !request.getCode().equalsIgnoreCase(promotion.getCode())
                && promotionRepository.existsByCode(request.getCode().toUpperCase())) {
            throw new AppException(ErrorCode.PROMOTION_CODE_EXISTS);
        }
        promotionMapper.updateEntity(request, promotion);
        promotion.setCode(normalizeCode(request.getCode()));
        return promotionMapper.toResponse(promotionRepository.save(promotion));
    }

    @Transactional
    public void delete(Long id) {
        Promotion promotion = findEntity(id);
        // Gỡ promotion khỏi các sản phẩm đang tham chiếu để không vướng FK.
        List<Product> products = productRepository.findByPromotion_PromotionId(id);
        products.forEach(p -> p.setPromotion(null));
        productRepository.saveAll(products);
        promotionRepository.delete(promotion);
    }

    /** Chi tiết promotion kèm danh sách sản phẩm được gán. */
    public PromotionResponse getById(Long id) {
        Promotion promotion = findEntity(id);
        PromotionResponse response = promotionMapper.toResponse(promotion);
        response.setProducts(buildProductList(promotion));
        return response;
    }

    public Page<PromotionResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate"));
        Page<Promotion> promotions = (keyword == null || keyword.isBlank())
                ? promotionRepository.findAll(pageable)
                : promotionRepository.searchByMultipleFields(keyword, pageable);
        return promotions.map(promotionMapper::toResponse);
    }

    @Transactional
    public List<PromotionProductResponse> assignProducts(Long promotionId, List<Long> productIds) {
        Promotion promotion = findEntity(promotionId);
        List<Product> products = productRepository.findAllById(productIds);
        if (products.isEmpty()) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        products.forEach(p -> p.setPromotion(promotion));
        productRepository.saveAll(products);
        return products.stream().map(p -> toProductResponse(p, promotion)).toList();
    }

    // ---------- helpers ----------

    private List<PromotionProductResponse> buildProductList(Promotion promotion) {
        return productRepository.findByPromotion_PromotionId(promotion.getPromotionId())
                .stream().map(p -> toProductResponse(p, promotion)).toList();
    }

    private PromotionProductResponse toProductResponse(Product product, Promotion promotion) {
        BigDecimal minPrice = skuRepository.findMinPriceByProductId(product.getId());
        if (minPrice == null) {
            minPrice = product.getPrice();
        }
        String imageUrl = imageRepository
                .findByEntityTypeAndEntityId(EntityType.PRODUCT, product.getId())
                .stream().findFirst()
                .map(img -> s3Service.resolveImageUrl(img.getImageUrl()))
                .orElse(null);
        return PromotionProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .minPrice(minPrice)
                .imageUrl(imageUrl)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .promotion(promotion.getTitle())
                .build();
    }

    private String normalizeCode(String code) {
        return code != null ? code.toUpperCase() : null;
    }

    private Promotion findEntity(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));
    }
}

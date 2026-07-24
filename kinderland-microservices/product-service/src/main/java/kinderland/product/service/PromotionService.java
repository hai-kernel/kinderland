package kinderland.product.service;

import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.product.mapper.PromotionMapper;
import kinderland.product.model.dto.request.PromotionRequest;
import kinderland.product.model.dto.response.PromotionProductResponse;
import kinderland.product.model.dto.response.PromotionResponse;
import kinderland.product.model.dto.response.PromotionValidationResponse;
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
import java.math.RoundingMode;
import java.time.LocalDateTime;
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
        promotion.setActive(request.getActive() == null || request.getActive());
        promotion.setUsedCount(0);
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
        // Các field ràng buộc dưới đây là OPTIONAL trong request. Form quản trị cũ không gửi
        // chúng, mà MapStruct ghi đè bằng null → mã đang giới hạn 100 lượt sẽ âm thầm thành
        // không giới hạn chỉ vì admin sửa lại tiêu đề. Giữ giá trị cũ khi request bỏ trống.
        Boolean previousActive = promotion.isActive();
        Integer previousUsageLimit = promotion.getUsageLimit();
        BigDecimal previousMinOrder = promotion.getMinOrderAmount();
        BigDecimal previousMaxDiscount = promotion.getMaxDiscountAmount();

        promotionMapper.updateEntity(request, promotion);
        promotion.setCode(normalizeCode(request.getCode()));
        promotion.setActive(request.getActive() != null ? request.getActive() : previousActive);
        if (request.getUsageLimit() == null) promotion.setUsageLimit(previousUsageLimit);
        if (request.getMinOrderAmount() == null) promotion.setMinOrderAmount(previousMinOrder);
        if (request.getMaxDiscountAmount() == null) promotion.setMaxDiscountAmount(previousMaxDiscount);

        return promotionMapper.toResponse(promotionRepository.save(promotion));
    }

    /**
     * NGUỒN SỰ THẬT DUY NHẤT cho việc áp mã: kiểm tra điều kiện + tính số tiền giảm.
     * FE gọi để hiển thị, order-service gọi để chốt số tiền thật khi tạo đơn — cùng một hàm,
     * nên số hiển thị và số lưu vào DB không thể lệch nhau.
     *
     * Trả về đối tượng có valid=false + message thay vì ném exception, để FE hiện được lý do
     * cụ thể ("hết lượt", "chưa đạt tối thiểu") ngay tại ô nhập mã.
     */
    public PromotionValidationResponse validate(String code, BigDecimal subtotal) {
        BigDecimal base = subtotal == null || subtotal.signum() < 0 ? BigDecimal.ZERO : subtotal;

        if (code == null || code.isBlank()) {
            return invalid(ErrorCode.PROMOTION_INVALID.getMessage(), base);
        }
        Promotion promotion = promotionRepository.findByCode(normalizeCode(code.trim())).orElse(null);
        if (promotion == null) {
            return invalid(ErrorCode.PROMOTION_INVALID.getMessage(), base);
        }

        LocalDateTime now = LocalDateTime.now();
        if (!promotion.isActive()) {
            return invalid(ErrorCode.PROMOTION_INACTIVE.getMessage(), base);
        }
        if (promotion.getStartDate() != null && now.isBefore(promotion.getStartDate())) {
            return invalid(ErrorCode.PROMOTION_NOT_STARTED.getMessage(), base);
        }
        if (promotion.getEndDate() != null && now.isAfter(promotion.getEndDate())) {
            return invalid(ErrorCode.PROMOTION_EXPIRED.getMessage(), base);
        }
        if (promotion.getUsageLimit() != null
                && promotion.getUsedCount() != null
                && promotion.getUsedCount() >= promotion.getUsageLimit()) {
            return invalid(ErrorCode.PROMOTION_USAGE_LIMIT_REACHED.getMessage(), base);
        }
        if (promotion.getMinOrderAmount() != null && base.compareTo(promotion.getMinOrderAmount()) < 0) {
            return invalid(ErrorCode.PROMOTION_MIN_ORDER_NOT_MET.getMessage()
                    + " (tối thiểu " + promotion.getMinOrderAmount().toBigInteger() + "đ)", base);
        }

        return PromotionValidationResponse.builder()
                .valid(true)
                .promotionId(promotion.getPromotionId())
                .code(promotion.getCode())
                .title(promotion.getTitle())
                .discountPercent(promotion.getDiscountPercent())
                .subtotal(base)
                .discountAmount(computeDiscount(promotion, base))
                .build();
    }

    /**
     * Ghi nhận đã dùng 1 lượt. CHỈ được gọi sau khi đơn/giao dịch thành công (order-service
     * gọi khi nhận PaymentCompletedEvent), không gọi lúc người dùng bấm "Áp dụng".
     * Trả false khi mã vừa hết lượt do đơn khác — caller quyết định xử lý, KHÔNG huỷ đơn đã trả tiền.
     */
    @Transactional
    public boolean redeem(Long promotionId) {
        return promotionRepository.incrementUsedCount(promotionId) > 0;
    }

    /** percent → tiền, kẹp trần maxDiscountAmount và không bao giờ vượt quá subtotal. */
    private BigDecimal computeDiscount(Promotion promotion, BigDecimal subtotal) {
        BigDecimal percent = promotion.getDiscountPercent() == null
                ? BigDecimal.ZERO : promotion.getDiscountPercent();
        BigDecimal discount = subtotal.multiply(percent)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

        if (promotion.getMaxDiscountAmount() != null
                && discount.compareTo(promotion.getMaxDiscountAmount()) > 0) {
            discount = promotion.getMaxDiscountAmount();
        }
        return discount.min(subtotal).max(BigDecimal.ZERO);
    }

    private PromotionValidationResponse invalid(String message, BigDecimal subtotal) {
        return PromotionValidationResponse.builder()
                .valid(false)
                .message(message)
                .subtotal(subtotal)
                .discountAmount(BigDecimal.ZERO)
                .build();
    }

    @Transactional
    public void delete(Long id) {
        Promotion promotion = findEntity(id);
        // Gỡ promotion khỏi các sản phẩm đang tham chiếu để không vướng FK.
        List<Product> products = productRepository.findByPromotion_PromotionIdAndDeletedFalse(id);
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
        return productRepository.findByPromotion_PromotionIdAndDeletedFalse(promotion.getPromotionId())
                .stream().map(p -> toProductResponse(p, promotion)).toList();
    }

    private PromotionProductResponse toProductResponse(Product product, Promotion promotion) {
        BigDecimal minPrice = skuRepository.findMinPriceByProductId(product.getId());
        if (minPrice == null) {
            minPrice = product.getPrice();
        }
        String imageUrl = imageRepository
                .findByEntityTypeAndEntityIdOrderByIdAsc(EntityType.PRODUCT, product.getId())
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

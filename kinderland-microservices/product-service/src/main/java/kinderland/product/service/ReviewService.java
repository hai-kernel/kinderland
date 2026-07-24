package kinderland.product.service;

import feign.FeignException;
import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.product.client.OrderClient;
import kinderland.product.model.dto.response.ReviewResponse;
import kinderland.product.model.entity.Review;
import kinderland.product.model.entity.Sku;
import kinderland.product.repository.ReviewRepository;
import kinderland.product.repository.SkuRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Đánh giá sản phẩm theo SKU. Ràng buộc "đã mua" kiểm tra cross-service qua Feign order-service.
 * Danh tính người đánh giá = email (GatewayAuthContext), không FK Account.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReviewService {

    ReviewRepository reviewRepository;
    SkuRepository skuRepository;
    OrderClient orderClient;

    @Transactional
    public ReviewResponse addReview(Long skuId, int rating, String comment, String accountEmail) {
        Sku sku = skuRepository.findById(skuId)
                .orElseThrow(() -> new AppException(ErrorCode.SKU_NOT_FOUND));

        if (!hasPurchased(accountEmail, skuId)) {
            throw new AppException(ErrorCode.NOT_PURCHASED);
        }
        if (reviewRepository.existsByAccountEmailAndSku_Id(accountEmail, skuId)) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }


        
        Review review = Review.builder()
                .accountEmail(accountEmail)
                .sku(sku)
                .rating(rating)
                .comment(comment)
                .build();
        return toResponse(reviewRepository.save(review));
    }

    @Transactional
    public ReviewResponse editReview(Long reviewId, int rating, String comment, String accountEmail) {
        Review review = findEntity(reviewId);
        if (!review.getAccountEmail().equals(accountEmail)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        review.setRating(rating);
        review.setComment(comment);
        return toResponse(reviewRepository.save(review));
    }

    public List<ReviewResponse> getReviewsBySku(Long skuId) {
        return reviewRepository.findBySku_IdOrderByCreatedAtDesc(skuId).stream().map(this::toResponse).toList();
    }

    public List<ReviewResponse> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId).stream().map(this::toResponse).toList();
    }

    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        reviewRepository.delete(findEntity(reviewId));
    }

    @Transactional
    public ReviewResponse replyToReview(Long reviewId, String reply) {
        Review review = findEntity(reviewId);
        review.setManagerReply(reply);
        review.setManagerReplyAt(LocalDateTime.now());
        return toResponse(reviewRepository.save(review));
    }

    // ---------- helpers ----------

    private boolean hasPurchased(String email, Long skuId) {
        try {
            return orderClient.hasPurchased(email, skuId);
        } catch (FeignException e) {
            log.error("Gọi Order Service (purchased-check) lỗi email={}, skuId={}: {}", email, skuId, e.getMessage());
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
        }
    }

    private ReviewResponse toResponse(Review review) {
        Sku sku = review.getSku();
        return ReviewResponse.builder()
                .id(review.getId())
                .accountId(null)
                .reviewerName(review.getAccountEmail())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .skuId(sku.getId())
                .skuCode(sku.getSkuCode())
                .size(sku.getSize())
                .color(sku.getColor())
                .productId(review.getProductId())
                .productName(sku.getProduct() != null ? sku.getProduct().getName() : null)
                .managerReply(review.getManagerReply())
                .managerReplyAt(review.getManagerReplyAt())
                .build();
    }

    private Review findEntity(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
    }
}

package kinderland.product.controller;

import jakarta.servlet.http.HttpServletRequest;
import kinderland.common.dto.BaseResponse;
import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.common.security.GatewayAuthContext;
import kinderland.product.model.dto.response.ReviewResponse;
import kinderland.product.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Đánh giá sản phẩm (khớp FE reviewApi.ts). rating/comment truyền qua query param như FE.
 * Đọc/ghi của khách: cần đăng nhập (đọc email qua GatewayAuthContext). Thao tác quản lý: ROLE_ADMIN/MANAGER.
 */
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/sku/{skuId}")
    public ResponseEntity<BaseResponse<List<ReviewResponse>>> getBySku(@PathVariable Long skuId, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK", reviewService.getReviewsBySku(skuId)));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<BaseResponse<List<ReviewResponse>>> getByProduct(@PathVariable Long productId, HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK", reviewService.getReviewsByProduct(productId)));
    }

    @PostMapping("/sku/{skuId}")
    public ResponseEntity<BaseResponse<ReviewResponse>> addReview(@PathVariable Long skuId,
                                                                  @RequestParam int rating,
                                                                  @RequestParam String comment,
                                                                  HttpServletRequest req) {
        ReviewResponse res = reviewService.addReview(skuId, rating, comment, currentEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.ok(201, req.getRequestURI(), "Review created successfully", res));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<BaseResponse<ReviewResponse>> edit(@PathVariable Long reviewId,
                                                             @RequestParam int rating,
                                                             @RequestParam String comment,
                                                             HttpServletRequest req) {
        ReviewResponse res = reviewService.editReview(reviewId, rating, comment, currentEmail());
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Review updated successfully", res));
    }

    // ---------- Manager ----------

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<BaseResponse<List<ReviewResponse>>> getAll(HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "OK", reviewService.getAllReviews()));
    }

    @PostMapping("/{reviewId}/reply")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<BaseResponse<ReviewResponse>> reply(@PathVariable Long reviewId,
                                                              @RequestParam String reply,
                                                              HttpServletRequest req) {
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Reply added successfully",
                reviewService.replyToReview(reviewId, reply)));
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long reviewId, HttpServletRequest req) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(BaseResponse.ok(200, req.getRequestURI(), "Review deleted successfully", null));
    }

    private String currentEmail() {
        String email = GatewayAuthContext.getCurrentEmail();
        if (email == null) {
            throw new AppException(ErrorCode.MISSING_TOKEN);
        }
        return email;
    }
}

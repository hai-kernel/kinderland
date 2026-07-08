package kinderland.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Tập error code dùng chung cho toàn hệ thống (giữ nguyên từ monolith).
 * Mỗi service chỉ dùng những code liên quan tới domain của mình, nhưng enum
 * được chia sẻ để client nhận message/format nhất quán.
 */
@Getter
public enum ErrorCode {
    EMAIL_INVALID("Email không hợp lệ.", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_FOUND("Tài khoản không tìm thấy.", HttpStatus.NOT_FOUND),
    RETURN_REQUEST_NOT_FOUND("Return request not found", HttpStatus.NOT_FOUND),
    ORDER_ITEM_NOT_FOUND("Order item not found", HttpStatus.NOT_FOUND),
    RETURN_ALREADY_EXISTS("Return request already exists for this order item", HttpStatus.CONFLICT),
    UNAUTHORIZED_RETURN("You can only return items from your own orders", HttpStatus.FORBIDDEN),
    RETURN_NOT_PENDING("Return request is not in pending status", HttpStatus.BAD_REQUEST),
    RETURN_NOT_APPROVED("Return request has not been approved", HttpStatus.BAD_REQUEST),
    RETURN_NOT_RECEIVED("Returned item has not been received", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_ACCESS("You are not authorized to access this resource", HttpStatus.FORBIDDEN),
    INVALID_REFUND_DETAILS("Invalid refund details provided", HttpStatus.BAD_REQUEST),

    PHONE_ALREADY_EXISTED("Phone number already exists", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTED("Email already exists", HttpStatus.BAD_REQUEST),
    ADDRESS_NOT_FOUND("Address not found", HttpStatus.NOT_FOUND),
    WISHLIST_NOT_FOUND("Wishlist not found", HttpStatus.NOT_FOUND),
    WISHLIST_ITEM_ALREADY_EXISTS("Product already exists in wishlist", HttpStatus.BAD_REQUEST),
    SKU_NOT_FOUND("SKU not found", HttpStatus.NOT_FOUND),
    WISHLIST_ITEM_NOT_FOUND("Wishlist item not found", HttpStatus.NOT_FOUND),
    REJECTION_REASON_REQUIRED("Rejection reason is required when rejecting a return request", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_FOUND("Product not found", HttpStatus.NOT_FOUND),
    WRONG_PASSWORD("Incorrect current password", HttpStatus.BAD_REQUEST),
    PASSWORD_DUPLICATED("The new password must be different from the old password", HttpStatus.BAD_REQUEST),
    PROMOTION_NOT_FOUND("Promotion not found", HttpStatus.NOT_FOUND),
    PROMOTION_CODE_EXISTS("Promotion code already exists", HttpStatus.CONFLICT),
    USERNAME_ALREADY_EXISTED("Username already exists", HttpStatus.CONFLICT),
    INVALID_TOKEN("Invalid or expired token", HttpStatus.UNAUTHORIZED),
    MISSING_TOKEN("Authentication token is required to access this resource", HttpStatus.UNAUTHORIZED),
    TOKEN_REQUIRED("Token is required", HttpStatus.BAD_REQUEST),
    PROMOTION_EXPIRED("Promotion expired", HttpStatus.CONFLICT),
    BLOG_NOT_FOUND("Blog not found", HttpStatus.NOT_FOUND),
    BLOG_CATEGORY_NOT_FOUND("Blog category not found", HttpStatus.NOT_FOUND),
    BLOG_CATEGORY_ALREADY_EXISTS("Blog category already exists", HttpStatus.CONFLICT),
    INSUFFICIENT_POINTS("Insufficient loyalty points", HttpStatus.BAD_REQUEST),
    STORE_NOT_FOUND("Store not found", HttpStatus.NOT_FOUND),
    INVENTORY_NOT_FOUND("Inventory not found", HttpStatus.NOT_FOUND),
    DELIVERY_NOT_FOUND("Delivery not found", HttpStatus.NOT_FOUND),
    REVIEW_NOT_FOUND("Review not found", HttpStatus.NOT_FOUND),
    NOT_PURCHASED("You can only review products you have purchased", HttpStatus.FORBIDDEN),
    REVIEW_ALREADY_EXISTS("You have already reviewed this product", HttpStatus.CONFLICT),
    // Lỗi giao tiếp giữa các service (Feign)
    SERVICE_UNAVAILABLE("Dịch vụ phụ thuộc tạm thời không khả dụng", HttpStatus.SERVICE_UNAVAILABLE),
    // Bổ sung cho product/order
    OUT_OF_STOCK("Sản phẩm không đủ tồn kho", HttpStatus.CONFLICT),
    CATEGORY_NOT_FOUND("Danh mục không tồn tại", HttpStatus.NOT_FOUND),
    ORDER_NOT_FOUND("Đơn hàng không tồn tại", HttpStatus.NOT_FOUND),
    INVALID_ORDER_STATUS("Trạng thái đơn hàng không hợp lệ cho thao tác này", HttpStatus.CONFLICT),
    CART_ITEM_NOT_FOUND("Sản phẩm không có trong giỏ hàng", HttpStatus.NOT_FOUND),
    CART_EMPTY("Giỏ hàng trống hoặc không có sản phẩm nào được chọn", HttpStatus.BAD_REQUEST),
    IMAGE_NOT_FOUND("Không tìm thấy ảnh", HttpStatus.NOT_FOUND),
    // Bổ sung cho payment-service
    PAYMENT_NOT_FOUND("Không tìm thấy thông tin thanh toán", HttpStatus.NOT_FOUND),
    PAYMENT_ALREADY_PAID("Đơn hàng đã được thanh toán", HttpStatus.CONFLICT),
    INVALID_PAYMENT_SIGNATURE("Chữ ký thanh toán không hợp lệ", HttpStatus.BAD_REQUEST),
    // Bổ sung cho auth-service
    REFRESH_TOKEN_REQUIRED("Refresh token is required", HttpStatus.BAD_REQUEST),
    GOOGLE_ID_TOKEN_REQUIRED("Google id token is required", HttpStatus.BAD_REQUEST),
    INVALID_GOOGLE_ID_TOKEN("Invalid Google ID token", HttpStatus.UNAUTHORIZED),
    GOOGLE_TOKEN_VERIFICATION_FAILED("Google token verification failed", HttpStatus.UNAUTHORIZED),
    EMAIL_NOT_FOUND("Email không tồn tại trong hệ thống.", HttpStatus.NOT_FOUND),
    INVALID_OTP("Mã OTP không hợp lệ.", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED("Mã OTP đã hết hạn.", HttpStatus.BAD_REQUEST),
    INVALID_REFRESH_TOKEN("Invalid refresh token", HttpStatus.UNAUTHORIZED),
    ;

    ErrorCode(String message, HttpStatus statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    private final String message;
    private final HttpStatus statusCode;
}

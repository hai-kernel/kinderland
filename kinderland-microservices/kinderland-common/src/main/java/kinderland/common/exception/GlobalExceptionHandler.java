package kinderland.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import kinderland.common.dto.BaseResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Xử lý exception tập trung, dùng chung cho mọi service qua auto-configuration
 * ({@code CommonAutoConfiguration}) — service không cần khai báo gì thêm.
 *
 * Lưu ý: KHÔNG còn handler cho BadCredentialsException ở đây (đó là lỗi đăng nhập,
 * chỉ phát sinh ở auth-service) để common KHÔNG phải phụ thuộc spring-security.
 * auth-service tự bổ sung handler đó.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<BaseResponse<Object>> handleAppException(AppException exception, HttpServletRequest request) {
        ErrorCode errorCode = exception.getErrorCode();
        int status = errorCode.getStatusCode().value();
        String msg = errorCode.getMessage();

        if (exception.getArgs() != null && exception.getArgs().length > 0) {
            msg = String.format(errorCode.getMessage(), exception.getArgs());
        }
        return ResponseEntity.status(errorCode.getStatusCode())
                .body(BaseResponse.error(status, request.getRequestURI(), msg, null));
    }

    /**
     * Vi phạm ràng buộc dữ liệu (khoá ngoại, unique...) — vd xoá brand khi vẫn còn product
     * trỏ tới nó. Không có handler này thì exception rơi xuống /error, trả 500 trần
     * {"status":500,"error":"Internal Server Error"} không kèm lý do; API Gateway lại
     * quy 500 thành 503 "dịch vụ không khả dụng" -> che mất lỗi thật, rất khó lần ra.
     *
     * Trả 409 CONFLICT kèm thông báo đọc được. KHÔNG lộ thông điệp gốc của database
     * (chứa tên bảng/cột) ra client; chỉ ghi vào log để điều tra.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponse<Object>> handleDataIntegrityViolation(
            DataIntegrityViolationException exception, HttpServletRequest request) {
        log.warn("Vi phạm ràng buộc dữ liệu tại {}: {}", request.getRequestURI(),
                exception.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(BaseResponse.error(HttpStatus.CONFLICT.value(), request.getRequestURI(),
                        "Dữ liệu đang được tham chiếu ở nơi khác nên không thể thực hiện thao tác này.",
                        null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Object>> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> errorMap = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String enumKey = error.getDefaultMessage();
            String errorMessage;
            try {
                errorMessage = ErrorCode.valueOf(enumKey).getMessage();
            } catch (IllegalArgumentException e) {
                errorMessage = enumKey;
            }
            errorMap.put(fieldName, errorMessage);
        });

        return ResponseEntity.badRequest()
                .body(BaseResponse.error(HttpStatus.BAD_REQUEST.value(), request.getRequestURI(),
                        "Dữ liệu đầu vào không hợp lệ", errorMap));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<Object>> handleJsonError(HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(BaseResponse.error(HttpStatus.BAD_REQUEST.value(), request.getRequestURI(),
                        "Định dạng JSON không hợp lệ.", null));
    }
}

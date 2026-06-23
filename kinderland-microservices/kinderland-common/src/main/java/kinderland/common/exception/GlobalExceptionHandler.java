package kinderland.common.exception;

import jakarta.servlet.http.HttpServletRequest;
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

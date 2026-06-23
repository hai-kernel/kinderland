package kinderland.common.exception;

import lombok.Getter;

/**
 * Exception nghiệp vụ dùng chung. Ném {@code throw new AppException(ErrorCode.X)}
 * và {@code GlobalExceptionHandler} sẽ map ra HTTP status + message tương ứng.
 */
@Getter
public class AppException extends RuntimeException {
    private final ErrorCode errorCode;
    private Object[] args;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, Object... args) {
        super(String.format(errorCode.getMessage(), args));
        this.errorCode = errorCode;
        this.args = args;
    }
}

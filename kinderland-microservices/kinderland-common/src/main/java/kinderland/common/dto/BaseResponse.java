package kinderland.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Wrapper response dùng chung cho TẤT CẢ service. Giữ nguyên format của monolith
 * để frontend không phải đổi gì.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Schema(description = "Base response wrapper for API responses")
public class BaseResponse<T> {

    @Schema(description = "Timestamp of the response", example = "2026-02-04T10:00:00Z")
    private String timestamp;

    @Schema(description = "HTTP status code of the response", example = "200")
    private int statusCode;

    @Schema(description = "API path of the request", example = "/api/v1/users")
    private String apiPath;

    @Schema(description = "Indicates whether the request was successful", example = "true")
    private boolean isSuccess;

    @Schema(description = "Errors of the response", example = "{'email': 'Email already exists'}")
    private Map<String, String> errors;

    @Schema(description = "Message describing the response", example = "Request successful")
    private String message;

    @Schema(description = "Payload of the response")
    private T data;

    public static <T> BaseResponse<T> ok(int statusCode, String apiPath, String message, T data) {
        return new BaseResponse<>(Instant.now().toString(), statusCode, apiPath, true, null, message, data);
    }

    public static <T> BaseResponse<T> error(int statusCode, String apiPath, String message, Map<String, String> errors) {
        return new BaseResponse<>(Instant.now().toString(), statusCode, apiPath, false, errors, message, null);
    }
}

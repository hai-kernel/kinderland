package kinderland.product.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BlogResponse {
    private Long id;
    private String authorEmail;
    private String title;
    private String content;
    private Long categoryId;
    private String categoryName;
    private boolean status;
    private int timeRead;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

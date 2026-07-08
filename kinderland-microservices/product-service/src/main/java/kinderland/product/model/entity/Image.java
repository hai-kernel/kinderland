package kinderland.product.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Ảnh lưu trên S3 (bucket private). imageUrl = KEY trên S3 (không phải URL public);
 * URL xem ảnh được sinh động qua presigned URL. entityType + entityId cho biết ảnh của đối tượng nào.
 */
@Entity
@Table(name = "images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** S3 object key (vd "images/uuid_tenfile.jpg"). */
    @Column(length = 1024)
    private String imageUrl;

    @Column(length = 512)
    private String fileName;

    @Enumerated(EnumType.STRING)
    private EntityType entityType;

    private Long entityId;

    private LocalDateTime createdAt;
}

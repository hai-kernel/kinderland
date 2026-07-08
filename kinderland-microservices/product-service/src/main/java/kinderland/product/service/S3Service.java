package kinderland.product.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface S3Service {
    /** Upload file lên S3, trả về object key. */
    String upload(MultipartFile file) throws IOException;

    /** Sinh presigned URL (link ký tạm) để xem ảnh private. */
    String generatePresignedUrl(String key);

    /** Xoá object trên S3 theo key. */
    void deleteFile(String key);
}

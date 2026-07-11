package kinderland.product.service.impl;

import kinderland.product.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Triển khai lưu ảnh trên S3 (port từ monolith).
 *  - upload: đẩy file lên bucket private, key dạng "images/{uuid}_{tên}".
 *  - generatePresignedUrl: sinh link ký tạm 60 phút để xem ảnh (bucket private không truy cập trực tiếp),
 *    có cache 50 phút để tránh ký lại liên tục.
 */
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    private record CachedUrl(String url, Instant expiresAt) {}
    private final Map<String, CachedUrl> urlCache = new ConcurrentHashMap<>();
    private static final Duration CACHE_TTL = Duration.ofMinutes(50);

    @Override
    public String upload(MultipartFile file) throws IOException {
        String key = "images/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
        return key;
    }

    @Override
    public String generatePresignedUrl(String key) {
        CachedUrl cached = urlCache.get(key);
        if (cached != null && Instant.now().isBefore(cached.expiresAt())) {
            return cached.url();
        }
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(60))
                .getObjectRequest(getObjectRequest)
                .build();
        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
        String url = presigned.url().toString();
        urlCache.put(key, new CachedUrl(url, Instant.now().plus(CACHE_TTL)));
        return url;
    }

    @Override
    public void deleteFile(String key) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.deleteObject(deleteRequest);
        urlCache.remove(key);
    }

    @Override
    public String resolveImageUrl(String imageUrlOrKey) {
        if (imageUrlOrKey == null || imageUrlOrKey.isBlank()) {
            return null;
        }
        if (imageUrlOrKey.startsWith("http")) {
            return imageUrlOrKey;   // đã là URL đầy đủ
        }
        return generatePresignedUrl(imageUrlOrKey);   // là S3 key -> ký tạm
    }
}

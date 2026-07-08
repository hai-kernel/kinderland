package kinderland.product.controller;

import jakarta.servlet.http.HttpServletRequest;
import kinderland.common.dto.BaseResponse;
import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.product.model.dto.response.ImageResponse;
import kinderland.product.model.entity.EntityType;
import kinderland.product.model.entity.Image;
import kinderland.product.repository.ImageRepository;
import kinderland.product.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Quản lý ảnh (lưu S3). Ghi (upload/xoá) = ADMIN; đọc yêu cầu đăng nhập.
 * Gateway đã route /api/v1/images/** sang product-service sẵn.
 */
@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final S3Service s3Service;
    private final ImageRepository imageRepository;

    /** Upload 1 ảnh (multipart) gắn cho 1 đối tượng (entityType + entityId). */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<ImageResponse>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam EntityType entityType,
            @RequestParam Long entityId,
            HttpServletRequest request) throws IOException {

        String key = s3Service.upload(file);
        Image image = imageRepository.save(Image.builder()
                .imageUrl(key)
                .fileName(file.getOriginalFilename())
                .entityType(entityType)
                .entityId(entityId)
                .createdAt(LocalDateTime.now())
                .build());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                BaseResponse.ok(HttpStatus.CREATED.value(), request.getRequestURI(),
                        "Tải ảnh thành công", toResponse(image)));
    }

    /** Lấy 1 ảnh theo id (kèm presigned URL). */
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ImageResponse>> getById(@PathVariable Long id, HttpServletRequest request) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.IMAGE_NOT_FOUND));
        return ResponseEntity.ok(BaseResponse.ok(HttpStatus.OK.value(), request.getRequestURI(), "OK", toResponse(image)));
    }

    /** Lấy toàn bộ ảnh của 1 đối tượng (vd tất cả ảnh của product id=1). */
    @GetMapping
    public ResponseEntity<BaseResponse<List<ImageResponse>>> listByEntity(
            @RequestParam EntityType entityType,
            @RequestParam Long entityId,
            HttpServletRequest request) {
        List<ImageResponse> images = imageRepository.findByEntityTypeAndEntityId(entityType, entityId)
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(BaseResponse.ok(HttpStatus.OK.value(), request.getRequestURI(), "OK", images));
    }

    /** Xoá ảnh (S3 + DB). */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id, HttpServletRequest request) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.IMAGE_NOT_FOUND));
        s3Service.deleteFile(image.getImageUrl());
        imageRepository.delete(image);
        return ResponseEntity.ok(BaseResponse.ok(HttpStatus.OK.value(), request.getRequestURI(), "Đã xoá ảnh", null));
    }

    private ImageResponse toResponse(Image image) {
        return ImageResponse.builder()
                .id(image.getId())
                .key(image.getImageUrl())
                .url(s3Service.generatePresignedUrl(image.getImageUrl()))
                .fileName(image.getFileName())
                .entityType(image.getEntityType() == null ? null : image.getEntityType().name())
                .entityId(image.getEntityId())
                .build();
    }
}

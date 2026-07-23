package kinderland.product.service;

import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.product.mapper.BrandMapper;
import kinderland.product.model.dto.request.BrandRequest;
import kinderland.product.model.dto.response.BrandResponse;
import kinderland.product.model.entity.Brand;
import kinderland.product.model.entity.EntityType;
import kinderland.product.model.entity.Image;
import kinderland.product.repository.BrandRepository;
import kinderland.product.repository.ImageRepository;
import kinderland.product.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BrandService {

    BrandRepository brandRepository;
    ProductRepository productRepository;
    ImageRepository imageRepository;
    S3Service s3Service;
    BrandMapper brandMapper;

    @Transactional
    public BrandResponse create(BrandRequest request) {
        Brand saved = brandRepository.save(brandMapper.toEntity(request));
        upsertBrandLogo(saved.getId(), request.getLogoUrl());
        return toResponse(saved);
    }

    @Transactional
    public BrandResponse update(Long id, BrandRequest request) {
        Brand brand = findEntity(id);
        brandMapper.updateEntity(request, brand);
        Brand saved = brandRepository.save(brand);
        upsertBrandLogo(saved.getId(), request.getLogoUrl());
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Brand brand = findEntity(id);
        if (productRepository.existsByBrandIdAndDeletedFalse(id)) {
            throw new AppException(ErrorCode.BRAND_IN_USE);
        }
        // images không có FK tới product_brands (quan hệ đa hình) nên database không tự dọn.
        // Không xoá tay thì dòng ảnh thành mồ côi, và brand mới trùng id sẽ "thừa kế" logo cũ.
        imageRepository.deleteAll(
                imageRepository.findByEntityTypeAndEntityIdOrderByIdAsc(EntityType.PRODUCT_BRAND, id));
        brandRepository.delete(brand);
    }

    public BrandResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    public List<BrandResponse> getAll() {
        return brandRepository.findAll().stream().map(this::toResponse).toList();
    }

    /** Map entity -> response + đính presigned URL của logo. */
    private BrandResponse toResponse(Brand brand) {
        BrandResponse response = brandMapper.toResponse(brand);
        // OrderByIdAsc: phải đọc ĐÚNG dòng mà upsertBrandLogo ghi vào. Truy vấn không
        // ORDER BY trả theo thứ tự vật lý, mà PostgreSQL đẩy dòng vừa UPDATE xuống cuối
        // bảng -> đổi logo xong lại hiển thị logo cũ.
        imageRepository.findByEntityTypeAndEntityIdOrderByIdAsc(EntityType.PRODUCT_BRAND, brand.getId())
                .stream().findFirst()
                .ifPresent(img -> response.setLogoUrl(s3Service.resolveImageUrl(img.getImageUrl())));
        return response;
    }

    /** Lưu/cập nhật logo thương hiệu (FE truyền S3 key qua request.logoUrl). */
    private void upsertBrandLogo(Long brandId, String key) {
        if (key == null || key.isBlank()) {
            return;   // không gửi key -> giữ nguyên logo hiện có
        }
        // CHẶN ghi đè bằng presigned URL: API trả logoUrl dạng presigned (hết hạn 60 phút);
        // client sửa thương hiệu mà không đổi ảnh có thể gửi lại chính giá trị đó. Lưu vào
        // DB thay cho S3 key -> sau 1 giờ logo chết vĩnh viễn.
        String lower = key.toLowerCase();
        if (lower.startsWith("http")
                && (lower.contains("x-amz-signature") || lower.contains("x-amz-credential"))) {
            log.warn("Bỏ qua logoUrl là presigned URL cho brand {} — giữ nguyên S3 key hiện có", brandId);
            return;
        }

        List<Image> existing =
                imageRepository.findByEntityTypeAndEntityIdOrderByIdAsc(EntityType.PRODUCT_BRAND, brandId);
        if (!existing.isEmpty()) {
            Image img = existing.get(0);
            img.setImageUrl(key);
            imageRepository.save(img);
        } else {
            imageRepository.save(Image.builder()
                    .entityType(EntityType.PRODUCT_BRAND)
                    .entityId(brandId)
                    .imageUrl(key)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }

    private Brand findEntity(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
    }
}

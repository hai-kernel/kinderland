package kinderland.product.service;

import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.product.model.dto.internal.SkuInternalResponse;
import kinderland.product.model.dto.request.SkuRequest;
import kinderland.product.model.dto.response.SkuResponse;
import kinderland.product.model.entity.EntityType;
import kinderland.product.model.entity.Product;
import kinderland.product.model.entity.Sku;
import kinderland.product.repository.ImageRepository;
import kinderland.product.repository.ProductRepository;
import kinderland.product.repository.SkuRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SkuService {

    SkuRepository skuRepository;
    ProductRepository productRepository;
    ImageRepository imageRepository;
    InventoryService inventoryService;
    S3Service s3Service;

    @Transactional
    public SkuResponse create(SkuRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        Sku sku = Sku.builder()
                .product(product)
                .skuCode(generateSkuCode())
                .size(request.getSize())
                .color(request.getColor())
                .type(request.getType())
                .price(request.getPrice())
                .build();
        return toResponse(skuRepository.save(sku));
    }

    @Transactional
    public SkuResponse update(Long id, SkuRequest request) {
        Sku sku = findEntity(id);
        if (request.getSkuCode() != null) sku.setSkuCode(request.getSkuCode());
        if (request.getSize() != null) sku.setSize(request.getSize());
        if (request.getColor() != null) sku.setColor(request.getColor());
        if (request.getType() != null) sku.setType(request.getType());
        if (request.getPrice() != null) sku.setPrice(request.getPrice());
        return toResponse(skuRepository.save(sku));
    }

    @Transactional
    public void delete(Long id) {
        skuRepository.delete(findEntity(id));
    }

    public SkuResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    /** Danh sách SKU công khai — LOẠI SKU của sản phẩm đã xoá mềm (xem SkuRepository). */
    public List<SkuResponse> getAll() {
        return skuRepository.findAllByProductNotDeleted().stream().map(this::toResponse).toList();
    }

    public List<SkuResponse> getByProduct(Long productId) {
        return skuRepository.findByProductId(productId).stream().map(this::toResponse).toList();
    }

    /** Internal API cho Order Service: giá SKU + tồn khả dụng tại 1 store. */
    public SkuInternalResponse getInternal(Long skuId, Long storeId) {
        Sku sku = findEntity(skuId);
        String imageUrl = resolveSkuImage(sku);
        return SkuInternalResponse.builder()
                .skuId(sku.getId())
                .skuCode(sku.getSkuCode())
                .size(sku.getSize())
                .color(sku.getColor())
                .price(sku.getPrice())
                .productId(sku.getProduct().getId())
                .productName(sku.getProduct().getName())
                .imageUrl(imageUrl)
                .availableQuantity(inventoryService.getAvailableQuantity(skuId, storeId))
                .build();
    }

    /** skuCode tự sinh dạng SKU001, SKU002... (như monolith). */
    private String generateSkuCode() {
        return "SKU" + String.format("%03d", skuRepository.count() + 1);
    }

    /**
     * Ảnh hiển thị cho SKU: ưu tiên ảnh RIÊNG của SKU, không có thì lấy ảnh bìa của product.
     *
     * Hầu hết SKU không có ảnh riêng (ảnh gắn ở PRODUCT), nên nếu chỉ tra EntityType.SKU thì
     * imageUrl luôn null -> giỏ hàng và danh sách SKU không có ảnh nào để hiện.
     */
    private String resolveSkuImage(Sku sku) {
        return imageRepository.findByEntityTypeAndEntityIdOrderByIdAsc(EntityType.SKU, sku.getId())
                .stream().findFirst()
                .or(() -> imageRepository
                        .findByEntityTypeAndEntityIdOrderByIdAsc(EntityType.PRODUCT, sku.getProduct().getId())
                        .stream().findFirst())
                .map(img -> s3Service.resolveImageUrl(img.getImageUrl()))
                .orElse(null);
    }

    private SkuResponse toResponse(Sku sku) {
        String imageUrl = resolveSkuImage(sku);
        return SkuResponse.builder()
                .id(sku.getId())
                .skuCode(sku.getSkuCode())
                .size(sku.getSize())
                .color(sku.getColor())
                .type(sku.getType())
                .imageUrl(imageUrl)
                .price(sku.getPrice())
                .productId(sku.getProduct().getId())
                .productName(sku.getProduct().getName())
                .build();
    }

    private Sku findEntity(Long id) {
        return skuRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SKU_NOT_FOUND));
    }
}

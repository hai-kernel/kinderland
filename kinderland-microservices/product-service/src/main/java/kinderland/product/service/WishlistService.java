package kinderland.product.service;

import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.product.model.dto.response.WishlistItemResponse;
import kinderland.product.model.dto.response.WishlistResponse;
import kinderland.product.model.entity.EntityType;
import kinderland.product.model.entity.Product;
import kinderland.product.model.entity.Wishlist;
import kinderland.product.model.entity.WishlistItem;
import kinderland.product.repository.ImageRepository;
import kinderland.product.repository.ProductRepository;
import kinderland.product.repository.WishlistRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WishlistService {

    WishlistRepository wishlistRepository;
    ProductRepository productRepository;
    // Ảnh sản phẩm nằm ở bảng `images` (entityType = PRODUCT), không phải cột trong
    // `products` — giống hệt cách ProductService.toResponse() lấy ảnh bìa.
    ImageRepository imageRepository;
    S3Service s3Service;

    public WishlistResponse getMyWishlist(String accountEmail) {
        Wishlist wishlist = wishlistRepository.findByAccountEmail(accountEmail)
                .orElseGet(() -> Wishlist.builder().accountEmail(accountEmail).build());
        return toResponse(wishlist);
    }

    @Transactional
    public WishlistResponse addItem(String accountEmail, Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        Wishlist wishlist = wishlistRepository.findByAccountEmail(accountEmail)
                .orElseGet(() -> wishlistRepository.save(Wishlist.builder().accountEmail(accountEmail).build()));

        boolean exists = wishlist.getItems().stream().anyMatch(i -> i.getProductId().equals(productId));
        if (exists) {
            throw new AppException(ErrorCode.WISHLIST_ITEM_ALREADY_EXISTS);
        }
        wishlist.getItems().add(WishlistItem.builder()
                .wishlist(wishlist)
                .productId(productId)
                .addedAt(LocalDateTime.now())
                .build());
        return toResponse(wishlistRepository.save(wishlist));
    }

    @Transactional
    public WishlistResponse removeItem(String accountEmail, Long itemId) {
        Wishlist wishlist = wishlistRepository.findByAccountEmail(accountEmail)
                .orElseThrow(() -> new AppException(ErrorCode.WISHLIST_NOT_FOUND));
        boolean removed = wishlist.getItems().removeIf(i -> i.getId().equals(itemId));
        if (!removed) {
            throw new AppException(ErrorCode.WISHLIST_ITEM_NOT_FOUND);
        }
        return toResponse(wishlistRepository.save(wishlist));
    }

    /** Map + enrich tên/giá/ảnh sản phẩm từ ProductRepository (co-located). */
    private WishlistResponse toResponse(Wishlist wishlist) {
        return WishlistResponse.builder()
                .accountEmail(wishlist.getAccountEmail())
                .items(wishlist.getItems().stream().map(item -> {
                    Product p = productRepository.findById(item.getProductId()).orElse(null);
                    return WishlistItemResponse.builder()
                            .id(item.getId())
                            .productId(item.getProductId())
                            .productName(p == null ? null : p.getName())
                            .imageUrl(resolveProductImage(item.getProductId()))
                            .price(p == null ? null : p.getPrice())
                            .addedAt(item.getAddedAt())
                            .build();
                }).toList())
                .build();
    }

    /** Ảnh bìa sản phẩm; null nếu sản phẩm chưa có ảnh -> FE hiển thị placeholder. */
    private String resolveProductImage(Long productId) {
        return imageRepository.findByEntityTypeAndEntityId(EntityType.PRODUCT, productId)
                .stream().findFirst()
                .map(img -> s3Service.resolveImageUrl(img.getImageUrl()))
                .orElse(null);
    }
}

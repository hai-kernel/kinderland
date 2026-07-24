package kinderland.order.service;

import feign.FeignException;
import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.order.client.ProductClient;
import kinderland.order.client.dto.SkuInternalResponse;
import kinderland.order.model.dto.request.AddToCartRequest;
import kinderland.order.model.dto.response.CartItemResponse;
import kinderland.order.model.dto.response.CartResponse;
import kinderland.order.model.entity.Cart;
import kinderland.order.model.entity.CartItem;
import kinderland.order.repository.CartRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Giỏ hàng theo mô hình SKU + Store (khớp FE).
 * Item lưu skuId + storeId; khi trả về enrich giá/tên/ảnh từ product-service (Feign).
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CartService {

    CartRepository cartRepository;
    ProductClient productClient;

    @Transactional
    public CartResponse addToCart(String accountEmail, AddToCartRequest request) {
        Cart cart = cartRepository.findByAccountEmail(accountEmail)
                .orElseGet(() -> cartRepository.save(Cart.builder().accountEmail(accountEmail).build()));

        // Gộp số lượng nếu cùng (sku, store) đã có trong giỏ.
        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getSkuId().equals(request.getSkuId()) && i.getStoreId().equals(request.getStoreId()))
                .findFirst().orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
        } else {
            cart.getItems().add(CartItem.builder()
                    .cart(cart)
                    .skuId(request.getSkuId())
                    .storeId(request.getStoreId())
                    .quantity(request.getQuantity())
                    .build());
        }
        return toResponse(cartRepository.save(cart));
    }

    /** Đặt lại số lượng của 1 dòng giỏ (theo cartItemId). */
    @Transactional
    public CartResponse updateItemQuantity(String accountEmail, Long cartItemId, int quantity) {
        Cart cart = requireCart(accountEmail);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));
        item.setQuantity(quantity);
        return toResponse(cartRepository.save(cart));
    }

    /** Xoá 1 dòng giỏ theo cartItemId. */
    @Transactional
    public CartResponse removeItem(String accountEmail, Long cartItemId) {
        Cart cart = requireCart(accountEmail);
        boolean removed = cart.getItems().removeIf(i -> i.getId().equals(cartItemId));
        if (!removed) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        return toResponse(cartRepository.save(cart));
    }

    public CartResponse getMyCart(String accountEmail) {
        Cart cart = cartRepository.findByAccountEmail(accountEmail)
                .orElseGet(() -> Cart.builder().accountEmail(accountEmail).build());
        return toResponse(cart);
    }

    @Transactional
    public void clearCart(String accountEmail) {
        cartRepository.findByAccountEmail(accountEmail).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Cart requireCart(String accountEmail) {
        return cartRepository.findByAccountEmail(accountEmail)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));
    }

    /** Map + enrich mỗi item bằng thông tin SKU (giá/tên/ảnh) từ product-service. */
    private CartResponse toResponse(Cart cart) {
        return CartResponse.builder()
                .id(cart.getId())
                .accountEmail(cart.getAccountEmail())
                .items(cart.getItems().stream().map(this::toItem).toList())
                .build();
    }

    private CartItemResponse toItem(CartItem item) {
        CartItemResponse.CartItemResponseBuilder b = CartItemResponse.builder()
                .id(item.getId())
                .skuId(item.getSkuId())
                .storeId(item.getStoreId())
                .quantity(item.getQuantity());
        try {
            SkuInternalResponse sku = productClient.getSku(item.getSkuId(), item.getStoreId());
            // price = giá SAU khuyến mãi (product-service đã áp); originalPrice = giá gốc.
            BigDecimal finalPrice = nz(sku.getPrice());
            BigDecimal originalPrice = sku.getOriginalPrice() == null ? finalPrice : sku.getOriginalPrice();
            BigDecimal qty = BigDecimal.valueOf(item.getQuantity());
            BigDecimal lineTotal = finalPrice.multiply(qty);
            BigDecimal lineDiscount = originalPrice.subtract(finalPrice).max(BigDecimal.ZERO).multiply(qty);

            b.skuCode(sku.getSkuCode())
                    .productName(sku.getProductName())
                    .imageUrl(sku.getImageUrl())
                    // price/finalPrice/totalPrice = số SAU giảm (giỏ cộng tiền theo đây).
                    .price(finalPrice)
                    .lineTotal(lineTotal)
                    .finalPrice(finalPrice)
                    .totalPrice(lineTotal)
                    // unitPrice = giá gốc để FE gạch ngang; discountAmount = tiền giảm CẢ DÒNG.
                    .unitPrice(originalPrice)
                    .discountAmount(lineDiscount);
        } catch (FeignException e) {
            log.warn("Không lấy được thông tin SKU {} khi hiển thị giỏ: {}", item.getSkuId(), e.getMessage());
        }
        return b.build();
    }
}

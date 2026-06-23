package kinderland.order.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kinderland.order.mapper.CartMapper;
import kinderland.order.model.dto.request.AddToCartRequest;
import kinderland.order.model.dto.response.CartResponse;
import kinderland.order.model.entity.Cart;
import kinderland.order.model.entity.CartItem;
import kinderland.order.repository.CartRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartService {

    CartRepository cartRepository;
    CartMapper cartMapper;

    @Transactional
    public CartResponse addToCart(String accountEmail, AddToCartRequest request) {
        Cart cart = cartRepository.findByAccountEmail(accountEmail)
                .orElseGet(() -> cartRepository.save(Cart.builder().accountEmail(accountEmail).build()));

        // Gộp số lượng nếu sản phẩm đã có trong giỏ.
        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
        } else {
            cart.getItems().add(CartItem.builder()
                    .cart(cart)
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    .build());
        }

        return cartMapper.toResponse(cartRepository.save(cart));
    }

    public CartResponse getMyCart(String accountEmail) {
        Cart cart = cartRepository.findByAccountEmail(accountEmail)
                .orElseGet(() -> Cart.builder().accountEmail(accountEmail).build());
        return cartMapper.toResponse(cart);
    }

    @Transactional
    public void clearCart(String accountEmail) {
        cartRepository.findByAccountEmail(accountEmail).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }
}

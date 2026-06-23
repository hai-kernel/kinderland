package kinderland.product.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import kinderland.product.model.dto.internal.ProductInternalResponse;
import kinderland.product.service.ProductService;

/**
 * Internal API — CHỈ dùng nội bộ (Order Service gọi qua OpenFeign theo lb://PRODUCT-SERVICE).
 *
 * Tiền tố /internal KHÔNG nằm trong route của API Gateway ⇒ không expose ra internet.
 * (Production: nên thêm bảo vệ network policy / shared-secret header / mTLS.)
 *
 * Trả thẳng DTO (không bọc BaseResponse) để Feign client map đơn giản.
 *
 * Việc TRỪ KHO không còn ở đây — đã chuyển sang Kafka consumer (OrderStockConsumer)
 * theo mô hình Saga bất đồng bộ (Order bắn order-events → Product trừ kho).
 */
@RestController
@RequestMapping("/internal/products")
@RequiredArgsConstructor
public class ProductInternalController {

    private final ProductService productService;

    /** Lấy giá/tồn kho 1 sản phẩm khi Order kiểm tra lúc tạo đơn (Feign, đồng bộ). */
    @GetMapping("/{id}")
    public ProductInternalResponse getProduct(@PathVariable Long id) {
        return productService.getInternal(id);
    }
}

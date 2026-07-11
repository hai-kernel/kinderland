package kinderland.product.service;

import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.common.security.GatewayAuthContext;
import kinderland.product.model.dto.request.InventoryRequest;
import kinderland.product.model.dto.response.InventoryItemResponse;
import kinderland.product.model.dto.response.StoreAvailabilityResponse;
import kinderland.product.model.entity.Inventory;
import kinderland.product.model.entity.Sku;
import kinderland.product.model.entity.Store;
import kinderland.product.repository.InventoryRepository;
import kinderland.product.repository.SkuRepository;
import kinderland.product.repository.StoreRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryService {

    InventoryRepository inventoryRepository;
    SkuRepository skuRepository;
    StoreRepository storeRepository;

    /** Nhập thêm kho (cộng dồn) cho store của manager đang đăng nhập. */
    @Transactional
    public void importStock(InventoryRequest request) {
        Inventory inv = getOrCreate(request.getSkuId(), currentManagerStore().getId());
        inv.setQuantity(inv.getQuantity() + request.getQuantity());
        inventoryRepository.save(inv);
    }

    /** Điều chỉnh (đặt LẠI số lượng đúng bằng giá trị kiểm kê). */
    @Transactional
    public void adjustStock(InventoryRequest request) {
        Inventory inv = getOrCreate(request.getSkuId(), currentManagerStore().getId());
        inv.setQuantity(Math.max(0, request.getQuantity()));
        inventoryRepository.save(inv);
    }

    /** Huỷ hàng hỏng (trừ kho). */
    @Transactional
    public void disposeStock(InventoryRequest request) {
        Inventory inv = getOrCreate(request.getSkuId(), currentManagerStore().getId());
        inv.setQuantity(Math.max(0, inv.getQuantity() - request.getQuantity()));
        inventoryRepository.save(inv);
    }

    /** Chuyển kho giữa 2 cửa hàng (admin). */
    @Transactional
    public void transferStock(Long fromStoreId, Long toStoreId, Long skuId, Integer quantity) {
        Inventory from = getOrCreate(skuId, fromStoreId);
        if (from.getQuantity() < quantity) {
            throw new AppException(ErrorCode.OUT_OF_STOCK);
        }
        from.setQuantity(from.getQuantity() - quantity);
        Inventory to = getOrCreate(skuId, toStoreId);
        to.setQuantity(to.getQuantity() + quantity);
        inventoryRepository.save(from);
        inventoryRepository.save(to);
    }

    /** Danh sách tồn kho của 1 store (storeId truyền vào, hoặc store của manager hiện tại). */
    public List<InventoryItemResponse> getInventory(Long storeId) {
        Long sid = (storeId != null) ? storeId : currentManagerStore().getId();
        return inventoryRepository.findByStoreId(sid).stream().map(this::toItem).toList();
    }

    /** Tình trạng còn hàng của 1 SKU tại tất cả cửa hàng (cho khách chọn cửa hàng). */
    public List<StoreAvailabilityResponse> getStoreAvailability(Long skuId) {
        Map<Long, Integer> qtyByStore = inventoryRepository.findBySkuId(skuId).stream()
                .collect(Collectors.toMap(inv -> inv.getStore().getId(), Inventory::getQuantity));
        return storeRepository.findByActiveTrue().stream().map(store -> {
            int qty = qtyByStore.getOrDefault(store.getId(), 0);
            return StoreAvailabilityResponse.builder()
                    .storeId(store.getId()).storeName(store.getName())
                    .address(store.getAddress()).latitude(store.getLatitude()).longitude(store.getLongitude())
                    .phone(store.getPhone()).openingTime(store.getOpeningTime()).closingTime(store.getClosingTime())
                    .quantity(qty)
                    .availabilityStatus(qty > 0 ? "IN_STOCK" : "OUT_OF_STOCK")
                    .build();
        }).toList();
    }

    /** Tồn kho khả dụng của 1 SKU tại 1 store (0 nếu chưa có bản ghi). Dùng cho order check lúc đặt. */
    public int getAvailableQuantity(Long skuId, Long storeId) {
        return inventoryRepository.findBySkuIdAndStoreId(skuId, storeId)
                .map(Inventory::getQuantity).orElse(0);
    }

    /** Trừ kho khi tạo đơn (theo sku+store). Ném OUT_OF_STOCK nếu không đủ. */
    @Transactional
    public void decrementStock(Long skuId, Long storeId, int quantity) {
        Inventory inv = inventoryRepository.findBySkuIdAndStoreId(skuId, storeId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));
        if (inv.getQuantity() < quantity) {
            throw new AppException(ErrorCode.OUT_OF_STOCK);
        }
        inv.setQuantity(inv.getQuantity() - quantity);
        inventoryRepository.save(inv);
    }

    /** Hoàn kho khi huỷ đơn (theo sku+store). */
    @Transactional
    public void restockStock(Long skuId, Long storeId, int quantity) {
        Inventory inv = getOrCreate(skuId, storeId);
        inv.setQuantity(inv.getQuantity() + quantity);
        inventoryRepository.save(inv);
    }

    private Inventory getOrCreate(Long skuId, Long storeId) {
        return inventoryRepository.findBySkuIdAndStoreId(skuId, storeId).orElseGet(() -> {
            Sku sku = skuRepository.findById(skuId)
                    .orElseThrow(() -> new AppException(ErrorCode.SKU_NOT_FOUND));
            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
            return inventoryRepository.save(Inventory.builder().sku(sku).store(store).quantity(0).build());
        });
    }

    private Store currentManagerStore() {
        String email = GatewayAuthContext.getCurrentEmail();
        if (email == null) {
            throw new AppException(ErrorCode.MISSING_TOKEN);
        }
        return storeRepository.findByManagerEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
    }

    private InventoryItemResponse toItem(Inventory inv) {
        Sku sku = inv.getSku();
        return InventoryItemResponse.builder()
                .id(inv.getId())
                .skuId(sku.getId()).skuCode(sku.getSkuCode())
                .productName(sku.getProduct().getName())
                .color(sku.getColor()).size(sku.getSize()).type(sku.getType())
                .quantity(inv.getQuantity())
                .storeId(inv.getStore().getId()).storeName(inv.getStore().getName())
                .build();
    }
}

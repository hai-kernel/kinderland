package kinderland.product.service;

import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.common.security.GatewayAuthContext;
import kinderland.product.model.dto.request.TransferCreateRequestDTO;
import kinderland.product.model.dto.response.TransferResponseDTO;
import kinderland.product.model.entity.Inventory;
import kinderland.product.model.entity.Sku;
import kinderland.product.model.entity.Store;
import kinderland.product.model.entity.TransferOrder;
import kinderland.product.model.entity.TransferStatus;
import kinderland.product.repository.InventoryRepository;
import kinderland.product.repository.SkuRepository;
import kinderland.product.repository.StoreRepository;
import kinderland.product.repository.TransferOrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Chuyển kho giữa 2 cửa hàng (state machine), port từ monolith.
 * Bỏ FK Account: createdBy = email; "cửa hàng của user" phân giải qua Store.managerEmail
 * (thay storeRepository.findByAccountId của monolith). Danh tính đọc từ GatewayAuthContext.
 *
 * Luồng: DRAFT → PENDING_APPROVAL → APPROVED → OUT_FOR_DELIVERY → RECEIVED → COMPLETED.
 * Trừ kho fromStore lúc ship; cộng kho toStore lúc complete.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class TransferService {

    TransferOrderRepository transferOrderRepository;
    StoreRepository storeRepository;
    SkuRepository skuRepository;
    InventoryRepository inventoryRepository;

    public TransferResponseDTO createDraft(TransferCreateRequestDTO request) {
        Store fromStore = getCurrentUserStore();
        Store toStore = storeRepository.findById(request.getToStoreId())
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        Sku sku = skuRepository.findById(request.getSkuId())
                .orElseThrow(() -> new AppException(ErrorCode.SKU_NOT_FOUND));

        TransferOrder order = TransferOrder.builder()
                .fromStore(fromStore)
                .toStore(toStore)
                .sku(sku)
                .quantity(request.getQuantity())
                .status(TransferStatus.DRAFT)
                .createdByEmail(currentEmail())
                .createdAt(LocalDateTime.now())
                .build();
        return map(transferOrderRepository.save(order));
    }

    public void sendRequest(Long id) {
        TransferOrder order = getOrder(id);
        validateFromStore(order);
        validateStatus(order, TransferStatus.DRAFT);
        order.setStatus(TransferStatus.PENDING_APPROVAL);
    }

    public void approve(Long id) {
        TransferOrder order = getOrder(id);
        validateToStore(order);
        validateStatus(order, TransferStatus.PENDING_APPROVAL);
        order.setStatus(TransferStatus.APPROVED);
    }

    public void reject(Long id) {
        TransferOrder order = getOrder(id);
        validateToStore(order);
        validateStatus(order, TransferStatus.PENDING_APPROVAL);
        order.setStatus(TransferStatus.REJECTED);
    }

    public void ship(Long id) {
        TransferOrder order = getOrder(id);
        validateFromStore(order);
        validateStatus(order, TransferStatus.APPROVED);

        Inventory fromInv = getOrCreateInventory(order.getFromStore(), order.getSku());
        if (fromInv.getQuantity() < order.getQuantity()) {
            throw new AppException(ErrorCode.OUT_OF_STOCK);
        }
        fromInv.setQuantity(fromInv.getQuantity() - order.getQuantity());
        inventoryRepository.save(fromInv);

        order.setStatus(TransferStatus.OUT_FOR_DELIVERY);
    }

    public void receive(Long id) {
        TransferOrder order = getOrder(id);
        validateToStore(order);
        validateStatus(order, TransferStatus.OUT_FOR_DELIVERY);
        order.setStatus(TransferStatus.RECEIVED);
    }

    public void complete(Long id) {
        TransferOrder order = getOrder(id);
        validateToStore(order);
        validateStatus(order, TransferStatus.RECEIVED);

        Inventory toInv = getOrCreateInventory(order.getToStore(), order.getSku());
        toInv.setQuantity(toInv.getQuantity() + order.getQuantity());
        inventoryRepository.save(toInv);

        order.setStatus(TransferStatus.COMPLETED);
    }

    public void lostDamaged(Long id) {
        TransferOrder order = getOrder(id);
        validateToStore(order);
        validateStatus(order, TransferStatus.OUT_FOR_DELIVERY);
        order.setStatus(TransferStatus.LOST_DAMAGED);
    }

    public List<TransferResponseDTO> getMyTransfers() {
        Store store = getCurrentUserStore();
        return transferOrderRepository.findByFromStore_IdOrToStore_Id(store.getId(), store.getId())
                .stream().map(this::map).toList();
    }

    // ---------- helpers ----------

    private TransferOrder getOrder(Long id) {
        return transferOrderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DELIVERY_NOT_FOUND));
    }

    private Store getCurrentUserStore() {
        return storeRepository.findByManagerEmail(currentEmail())
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
    }

    private Inventory getOrCreateInventory(Store store, Sku sku) {
        return inventoryRepository.findBySkuIdAndStoreId(sku.getId(), store.getId())
                .orElseGet(() -> inventoryRepository.save(Inventory.builder()
                        .store(store).sku(sku).quantity(0).build()));
    }

    private void validateFromStore(TransferOrder order) {
        if (!order.getFromStore().getId().equals(getCurrentUserStore().getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    private void validateToStore(TransferOrder order) {
        if (!order.getToStore().getId().equals(getCurrentUserStore().getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    private void validateStatus(TransferOrder order, TransferStatus expected) {
        if (order.getStatus() != expected) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }
    }

    private TransferResponseDTO map(TransferOrder o) {
        return TransferResponseDTO.builder()
                .id(o.getId())
                .fromStoreName(o.getFromStore().getName())
                .toStoreName(o.getToStore().getName())
                .skuCode(o.getSku().getSkuCode())
                .quantity(o.getQuantity())
                .status(o.getStatus())
                .createdBy(o.getCreatedByEmail())
                .build();
    }

    private String currentEmail() {
        String email = GatewayAuthContext.getCurrentEmail();
        if (email == null) {
            throw new AppException(ErrorCode.MISSING_TOKEN);
        }
        return email;
    }
}

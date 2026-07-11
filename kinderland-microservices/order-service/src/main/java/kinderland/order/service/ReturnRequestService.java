package kinderland.order.service;

import feign.FeignException;
import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.order.client.StoreClient;
import kinderland.order.client.dto.StoreInternalResponse;
import kinderland.order.event.OrderEventPublisher;
import kinderland.order.event.ReturnReceivedEvent;
import kinderland.order.model.dto.request.RefundRequestDTO;
import kinderland.order.model.dto.request.RejectReturnRequestDTO;
import kinderland.order.model.dto.request.ReturnRequestDTO;
import kinderland.order.model.dto.response.ReturnResponseDTO;
import kinderland.order.model.entity.Order;
import kinderland.order.model.entity.OrderItem;
import kinderland.order.model.entity.ReturnRequest;
import kinderland.order.model.entity.ReturnStatus;
import kinderland.order.repository.OrderItemRepository;
import kinderland.order.repository.ReturnRequestRepository;
import kinderland.order.util.ShippingLabelTemplate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Yêu cầu trả hàng/hoàn tiền, port từ monolith nhưng phá coupling FK:
 * customer/processedBy = email (String); thông tin store enrich qua Feign product-service;
 * hoàn kho khi RECEIVED phát ReturnReceivedEvent → product-service. Hoàn tiền là chuyển khoản
 * thủ công (quản lý nhập mã giao dịch) — KHÔNG gọi payment-service, đúng như monolith.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReturnRequestService {

    ReturnRequestRepository returnRequestRepository;
    OrderItemRepository orderItemRepository;
    LoyaltyService loyaltyService;
    StoreClient storeClient;
    OrderEventPublisher orderEventPublisher;
    ShippingLabelTemplate shippingLabelTemplate;

    public Page<ReturnResponseDTO> getAllReturnRequests(Pageable pageable) {
        return returnRequestRepository.findAll(pageable).map(this::mapToDTO);
    }

    public ReturnResponseDTO getReturnRequestById(Long id) {
        return mapToDTO(findEntity(id));
    }

    public Page<ReturnResponseDTO> getMyReturnRequests(Pageable pageable, String email) {
        return returnRequestRepository.findByCustomerEmail(email, pageable).map(this::mapToDTO);
    }

    @Transactional
    public ReturnResponseDTO createReturnRequest(ReturnRequestDTO request, String customerEmail) {
        OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_ITEM_NOT_FOUND));

        if (returnRequestRepository.existsByOrderItem_Id(orderItem.getId())) {
            throw new AppException(ErrorCode.RETURN_ALREADY_EXISTS);
        }
        if (!orderItem.getOrder().getAccountEmail().equals(customerEmail)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_RETURN);
        }

        ReturnRequest returnRequest = ReturnRequest.builder()
                .returnCode(generateReturnCode())
                .orderItem(orderItem)
                .customerEmail(customerEmail)
                .returnReason(request.getReturnReason())
                .description(request.getDescription())
                .photoUrls(request.getPhotoUrls() == null ? new ArrayList<>()
                        : new ArrayList<>(request.getPhotoUrls().stream().filter(u -> u != null && !u.isBlank()).toList()))
                .bankAccountNumber(request.getBankAccountNumber())
                .bankName(request.getBankName())
                .bankAccountName(request.getBankAccountName())
                .refundAmount(orderItem.getLineTotal())
                .refundType("BANK_TRANSFER")
                .returnStatus(ReturnStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();

        return mapToDTO(returnRequestRepository.save(returnRequest));
    }

    @Transactional
    public ReturnResponseDTO approveReturn(Long id, String processedByEmail) {
        ReturnRequest returnRequest = findEntity(id);
        if (returnRequest.getReturnStatus() != ReturnStatus.PENDING) {
            throw new AppException(ErrorCode.RETURN_NOT_PENDING);
        }
        returnRequest.setReturnStatus(ReturnStatus.APPROVED);
        returnRequest.setProcessedByEmail(processedByEmail);
        returnRequest.setProcessedAt(LocalDateTime.now());
        return mapToDTO(returnRequestRepository.save(returnRequest));
    }

    @Transactional
    public ReturnResponseDTO rejectReturn(Long id, RejectReturnRequestDTO request, String processedByEmail) {
        ReturnRequest returnRequest = findEntity(id);
        if (returnRequest.getReturnStatus() != ReturnStatus.PENDING) {
            throw new AppException(ErrorCode.RETURN_NOT_PENDING);
        }
        if (request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
            throw new AppException(ErrorCode.REJECTION_REASON_REQUIRED);
        }
        returnRequest.setReturnStatus(ReturnStatus.REJECTED);
        returnRequest.setRejectionReason(request.getRejectionReason());
        returnRequest.setProcessedByEmail(processedByEmail);
        returnRequest.setProcessedAt(LocalDateTime.now());
        return mapToDTO(returnRequestRepository.save(returnRequest));
    }

    @Transactional
    public ReturnResponseDTO markReceived(Long id, String processedByEmail) {
        ReturnRequest returnRequest = findEntity(id);
        if (returnRequest.getReturnStatus() != ReturnStatus.APPROVED) {
            throw new AppException(ErrorCode.RETURN_NOT_APPROVED);
        }
        returnRequest.setReturnStatus(ReturnStatus.RECEIVED);
        returnRequest.setProcessedByEmail(processedByEmail);
        returnRequest.setProcessedAt(LocalDateTime.now());
        ReturnRequest saved = returnRequestRepository.save(returnRequest);

        // Hoàn kho theo (sku, store) qua event → product-service cộng lại tồn.
        OrderItem orderItem = saved.getOrderItem();
        orderEventPublisher.publishReturnReceived(ReturnReceivedEvent.builder()
                .returnId(saved.getReturnId())
                .skuId(orderItem.getSkuId())
                .storeId(orderItem.getOrder().getStoreId())
                .quantity(orderItem.getQuantity())
                .build());

        return mapToDTO(saved);
    }

    @Transactional
    public ReturnResponseDTO refund(Long id, RefundRequestDTO request, String processedByEmail) {
        ReturnRequest returnRequest = findEntity(id);
        if (returnRequest.getReturnStatus() != ReturnStatus.RECEIVED) {
            throw new AppException(ErrorCode.RETURN_NOT_RECEIVED);
        }
        if (returnRequest.getRefundAmount() == null) {
            throw new AppException(ErrorCode.INVALID_REFUND_DETAILS);
        }

        // Quản lý chuyển khoản thủ công, nhập mã giao dịch làm bằng chứng.
        returnRequest.setRefundType(request.getRefundType());
        returnRequest.setRefundTransactionCode(request.getBankTransactionCode());

        // Trừ lại điểm loyalty đã tích cho phần tiền được hoàn.
        loyaltyService.deductPoints(returnRequest.getCustomerEmail(), returnRequest.getRefundAmount());

        returnRequest.setReturnStatus(ReturnStatus.REFUNDED);
        returnRequest.setProcessedByEmail(processedByEmail);
        returnRequest.setRefundedAt(LocalDateTime.now());
        return mapToDTO(returnRequestRepository.save(returnRequest));
    }

    public String generateShippingLabel(Long id) {
        ReturnRequest returnRequest = findEntity(id);
        if (returnRequest.getReturnStatus() != ReturnStatus.APPROVED
                && returnRequest.getReturnStatus() != ReturnStatus.RECEIVED
                && returnRequest.getReturnStatus() != ReturnStatus.REFUNDED) {
            throw new AppException(ErrorCode.RETURN_NOT_APPROVED);
        }

        OrderItem orderItem = returnRequest.getOrderItem();
        Order order = orderItem.getOrder();
        StoreInternalResponse store = fetchStore(order.getStoreId());

        Map<String, String> values = new HashMap<>();
        values.put("returnCode", nvl(returnRequest.getReturnCode()));
        values.put("orderId", "ORD-" + order.getId());
        values.put("customerName", nvl(returnRequest.getCustomerEmail()));
        values.put("customerPhone", "N/A");
        values.put("storeName", store != null ? nvl(store.getName()) : "N/A");
        values.put("storeAddress", store != null ? nvl(store.getAddress()) : "N/A");
        values.put("storePhone", store != null && store.getPhone() != null ? store.getPhone() : "N/A");
        values.put("productName", nvl(orderItem.getProductName()));
        values.put("quantity", String.valueOf(orderItem.getQuantity()));
        return shippingLabelTemplate.render(values);
    }

    // ---------- helpers ----------

    private ReturnResponseDTO mapToDTO(ReturnRequest entity) {
        OrderItem orderItem = entity.getOrderItem();
        Order order = orderItem.getOrder();
        StoreInternalResponse store = fetchStore(order.getStoreId());

        return ReturnResponseDTO.builder()
                .returnId(entity.getReturnId())
                .returnCode(entity.getReturnCode())
                .orderItemId(orderItem.getId())
                .orderId(order.getId())
                .returnReason(entity.getReturnReason())
                .rejectionReason(entity.getRejectionReason())
                .returnStatus(entity.getReturnStatus())
                .description(entity.getDescription())
                .photoUrls(entity.getPhotoUrls())
                .refundAmount(entity.getRefundAmount())
                .refundType(entity.getRefundType())
                .bankAccountNumber(entity.getBankAccountNumber())
                .bankName(entity.getBankName())
                .bankAccountName(entity.getBankAccountName())
                .refundTransactionCode(entity.getRefundTransactionCode())
                .requestedAt(entity.getRequestedAt())
                .processedAt(entity.getProcessedAt())
                .refundedAt(entity.getRefundedAt())
                .customerEmail(entity.getCustomerEmail())
                .customerName(null)   // PII (tên/điện thoại) thuộc auth-service, không replicate
                .customerPhone(null)
                .productName(orderItem.getProductName())
                .quantity(orderItem.getQuantity())
                .storeName(store != null ? store.getName() : null)
                .storeAddress(store != null ? store.getAddress() : null)
                .storePhone(store != null ? store.getPhone() : null)
                .processedByName(entity.getProcessedByEmail())
                .build();
    }

    /** Lấy store qua Feign; best-effort (không chặn nghiệp vụ nếu product-service lỗi). */
    private StoreInternalResponse fetchStore(Long storeId) {
        if (storeId == null) {
            return null;
        }
        try {
            return storeClient.getStore(storeId);
        } catch (FeignException e) {
            log.warn("Không lấy được store {} từ product-service: {}", storeId, e.getMessage());
            return null;
        }
    }

    private String generateReturnCode() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = returnRequestRepository.count() + 1;
        return String.format("RET-%s-%03d", dateStr, count);
    }

    private ReturnRequest findEntity(Long id) {
        return returnRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RETURN_REQUEST_NOT_FOUND));
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}

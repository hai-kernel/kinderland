package kinderland.product.model.dto.response;

import kinderland.product.model.entity.TransferStatus;
import lombok.Builder;
import lombok.Data;

/** Khớp FE StockTransferPage Transfer {id, fromStoreName, toStoreName, skuCode, quantity, status, createdBy}. */
@Data
@Builder
public class TransferResponseDTO {
    private Long id;
    private String fromStoreName;
    private String toStoreName;
    private String skuCode;
    private Integer quantity;
    private TransferStatus status;
    private String createdBy;
}

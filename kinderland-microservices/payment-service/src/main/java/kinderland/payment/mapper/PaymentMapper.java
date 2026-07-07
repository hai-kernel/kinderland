package kinderland.payment.mapper;

import kinderland.payment.model.dto.response.PaymentResponse;
import kinderland.payment.model.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Payment -> PaymentResponse. status/method (enum) -> String map tự động theo tên hằng.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PaymentMapper {
    PaymentResponse toResponse(Payment payment);
}

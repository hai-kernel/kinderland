package kinderland.product.mapper;

import kinderland.product.model.dto.request.PromotionRequest;
import kinderland.product.model.dto.response.PromotionResponse;
import kinderland.product.model.entity.Promotion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PromotionMapper {

    /** Chỉ map field vô hướng; danh sách products do service enrich. */
    @Mapping(target = "products", ignore = true)
    PromotionResponse toResponse(Promotion promotion);

    // code do service set (upper-case); promotionId do DB sinh.
    // active: request là Boolean nullable còn entity là boolean primitive — để MapStruct map
    // sẽ NPE khi request bỏ trống, nên service set tay (mặc định true).
    // usedCount: chỉ tăng qua redeem() sau khi thanh toán thành công, KHÔNG cho client set.
    @Mapping(target = "promotionId", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "usedCount", ignore = true)
    Promotion toEntity(PromotionRequest request);

    @Mapping(target = "promotionId", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "usedCount", ignore = true)
    void updateEntity(PromotionRequest request, @MappingTarget Promotion promotion);
}

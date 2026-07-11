package kinderland.product.mapper;

import kinderland.product.model.dto.request.StoreRequest;
import kinderland.product.model.dto.response.StoreResponse;
import kinderland.product.model.entity.Store;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface StoreMapper {

    StoreResponse toResponse(Store store);

    List<StoreResponse> toResponseList(List<Store> stores);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Store toEntity(StoreRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(StoreRequest request, @MappingTarget Store store);
}

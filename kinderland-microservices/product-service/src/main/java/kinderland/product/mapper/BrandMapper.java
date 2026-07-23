package kinderland.product.mapper;

import kinderland.product.model.dto.request.BrandRequest;
import kinderland.product.model.dto.response.BrandResponse;
import kinderland.product.model.entity.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BrandMapper {

    // logoUrl KHÔNG có trên entity Brand (ảnh nằm ở bảng images, quan hệ đa hình).
    // BrandService tự đính presigned URL sau khi map — giống cách ProductMapper làm với imageUrl.
    @Mapping(target = "logoUrl", ignore = true)
    BrandResponse toResponse(Brand brand);

    List<BrandResponse> toResponseList(List<Brand> brands);

    // logoUrl là S3 key gửi kèm request, KHÔNG phải cột của Brand -> service xử lý riêng.
    @Mapping(target = "id", ignore = true)
    Brand toEntity(BrandRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntity(BrandRequest request, @MappingTarget Brand brand);
}

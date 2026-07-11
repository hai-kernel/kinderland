package kinderland.product.mapper;

import kinderland.product.model.dto.request.BlogCategoryRequest;
import kinderland.product.model.dto.response.BlogCategoryResponse;
import kinderland.product.model.entity.BlogCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BlogCategoryMapper {

    BlogCategoryResponse toResponse(BlogCategory category);

    List<BlogCategoryResponse> toResponseList(List<BlogCategory> categories);

    @Mapping(target = "id", ignore = true)
    BlogCategory toEntity(BlogCategoryRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntity(BlogCategoryRequest request, @MappingTarget BlogCategory category);
}

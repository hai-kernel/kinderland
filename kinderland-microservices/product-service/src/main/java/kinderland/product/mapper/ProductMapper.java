package kinderland.product.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import kinderland.product.model.dto.internal.ProductInternalResponse;
import kinderland.product.model.dto.request.ProductRequest;
import kinderland.product.model.dto.response.ProductResponse;
import kinderland.product.model.entity.Product;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProductMapper {

    // Lấy id/tên danh mục & thương hiệu từ quan hệ lồng nhau (MapStruct tự null-check).
    // minPrice = price (FE đọc minPrice); imageUrl do service enrich (resolve presigned URL).
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "brandId", source = "brand.id")
    @Mapping(target = "brandName", source = "brand.name")
    @Mapping(target = "minPrice", source = "price")
    @Mapping(target = "imageUrl", ignore = true)
    ProductResponse toResponse(Product product);

    List<ProductResponse> toResponseList(List<Product> products);

    ProductInternalResponse toInternal(Product product);

    // category/brand & active do service set; id do DB sinh.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "promotion", ignore = true)
    Product toEntity(ProductRequest request);

    // IGNORE null: FE cập nhật gửi thiếu field (vd price) sẽ KHÔNG ghi đè null lên giá trị cũ.
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "promotion", ignore = true)
    void updateEntity(ProductRequest request, @MappingTarget Product product);
}

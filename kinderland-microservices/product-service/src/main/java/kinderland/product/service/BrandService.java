package kinderland.product.service;

import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.product.mapper.BrandMapper;
import kinderland.product.model.dto.request.BrandRequest;
import kinderland.product.model.dto.response.BrandResponse;
import kinderland.product.model.entity.Brand;
import kinderland.product.repository.BrandRepository;
import kinderland.product.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BrandService {

    BrandRepository brandRepository;
    ProductRepository productRepository;
    BrandMapper brandMapper;

    @Transactional
    public BrandResponse create(BrandRequest request) {
        return brandMapper.toResponse(brandRepository.save(brandMapper.toEntity(request)));
    }

    @Transactional
    public BrandResponse update(Long id, BrandRequest request) {
        Brand brand = findEntity(id);
        brandMapper.updateEntity(request, brand);
        return brandMapper.toResponse(brandRepository.save(brand));
    }

    @Transactional
    public void delete(Long id) {
        Brand brand = findEntity(id);
        if (productRepository.existsByBrandId(id)) {
            throw new AppException(ErrorCode.BRAND_IN_USE);
        }
        brandRepository.delete(brand);
    }

    public BrandResponse getById(Long id) {
        return brandMapper.toResponse(findEntity(id));
    }

    public List<BrandResponse> getAll() {
        return brandMapper.toResponseList(brandRepository.findAll());
    }

    private Brand findEntity(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
    }
}

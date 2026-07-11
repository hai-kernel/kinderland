package kinderland.product.service;

import kinderland.common.exception.AppException;
import kinderland.common.exception.ErrorCode;
import kinderland.product.mapper.StoreMapper;
import kinderland.product.model.dto.internal.StoreInternalResponse;
import kinderland.product.model.dto.request.StoreRequest;
import kinderland.product.model.dto.response.NearbyStoreResponse;
import kinderland.product.model.dto.response.StoreResponse;
import kinderland.product.model.entity.Store;
import kinderland.product.repository.StoreRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StoreService {

    StoreRepository storeRepository;
    StoreMapper storeMapper;

    @Transactional
    public StoreResponse create(StoreRequest request) {
        return storeMapper.toResponse(storeRepository.save(storeMapper.toEntity(request)));
    }

    @Transactional
    public StoreResponse update(Long id, StoreRequest request) {
        Store store = findEntity(id);
        storeMapper.updateEntity(request, store);
        return storeMapper.toResponse(storeRepository.save(store));
    }

    @Transactional
    public void delete(Long id) {
        storeRepository.delete(findEntity(id));
    }

    public StoreResponse getById(Long id) {
        return storeMapper.toResponse(findEntity(id));
    }

    public List<StoreResponse> getAll() {
        return storeMapper.toResponseList(storeRepository.findAll());
    }

    /** Internal API cho order-service (Feign): thông tin store tối thiểu để hiển thị return. */
    public StoreInternalResponse getInternal(Long id) {
        Store store = findEntity(id);
        return StoreInternalResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .address(store.getAddress())
                .phone(store.getPhone())
                .build();
    }

    public List<StoreResponse> search(String keyword) {
        return storeMapper.toResponseList(storeRepository.search(keyword == null ? "" : keyword));
    }

    /** Cửa hàng gần toạ độ (lat,lng), sắp theo khoảng cách tăng dần (Haversine). */
    public List<NearbyStoreResponse> nearby(double lat, double lng) {
        return storeRepository.findByActiveTrue().stream()
                .filter(s -> s.getLatitude() != null && s.getLongitude() != null)
                .map(s -> NearbyStoreResponse.builder()
                        .id(s.getId()).name(s.getName()).code(s.getCode())
                        .address(s.getAddress()).phone(s.getPhone())
                        .latitude(s.getLatitude()).longitude(s.getLongitude())
                        .distanceKm(haversine(lat, lng, s.getLatitude(), s.getLongitude()))
                        .build())
                .sorted(Comparator.comparingDouble(NearbyStoreResponse::getDistanceKm))
                .toList();
    }

    /** Khoảng cách 2 điểm trên mặt cầu (km). */
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(R * c * 100.0) / 100.0;   // làm tròn 2 chữ số
    }

    private Store findEntity(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
    }
}

package kinderland.product.repository;

import kinderland.product.model.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    List<Store> findByActiveTrue();

    /** Cửa hàng mà user (email) đang quản lý — để nhập/điều chỉnh kho theo store của mình. */
    Optional<Store> findByManagerEmail(String managerEmail);

    /** Tìm cửa hàng theo tên / mã / địa chỉ (không phân biệt hoa thường). */
    @Query("""
            select s from Store s
            where lower(s.name) like lower(concat('%', :kw, '%'))
               or lower(s.code) like lower(concat('%', :kw, '%'))
               or lower(s.address) like lower(concat('%', :kw, '%'))
            """)
    List<Store> search(@Param("kw") String keyword);
}

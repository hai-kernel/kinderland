package kinderland.auth.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import kinderland.auth.model.entity.Account;
import kinderland.auth.model.entity.Address;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByAccount(Account account);
}

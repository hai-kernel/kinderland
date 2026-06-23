package kinderland.auth.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import kinderland.auth.model.entity.Account;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<Account> findByEmail(String email);
}

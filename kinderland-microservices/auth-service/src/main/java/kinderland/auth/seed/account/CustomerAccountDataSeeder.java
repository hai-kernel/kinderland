package kinderland.auth.seed.account;

import kinderland.auth.model.entity.Account;
import kinderland.auth.repo.AccountRepository;
import kinderland.common.seed.AbstractDataSeeder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Order(30)
@ConditionalOnProperty(name = "app.seed.account.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class CustomerAccountDataSeeder extends AbstractDataSeeder {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String getName() {
        return "CustomerAccountDataSeeder";
    }

    @Override
    @Transactional
    public void seed() {
        logStart();
        
        String defaultPassword = System.getenv("SEED_DEFAULT_PASSWORD");
        if (defaultPassword == null || defaultPassword.isEmpty()) {
            defaultPassword = "Seed@123456";
        }
        
        String encodedPassword = passwordEncoder.encode(defaultPassword);
        
        int created = 0;
        int skipped = 0;

        for (int i = 1; i <= 35; i++) {
            String email = String.format("customer%02d@kinderland.vn", i);
            String username = String.format("customer%02d", i);
            String firstName = "Customer";
            String lastName = String.format("%02d", i);

            if (accountRepository.existsByEmail(email)) {
                skipped++;
                continue;
            }

            Account account = Account.builder()
                    .username(username)
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .password(encodedPassword)
                    .role(Account.Role.CUSTOMER)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            accountRepository.save(account);
            log.info("Created user: {}", email);
            created++;
        }

        logCompleted(created, skipped);
    }
}

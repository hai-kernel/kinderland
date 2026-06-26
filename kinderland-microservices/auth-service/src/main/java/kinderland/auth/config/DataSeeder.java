package kinderland.auth.config;

import kinderland.auth.model.entity.Account;
import kinderland.auth.repo.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Seeds essential accounts on startup if they do not already exist.
 *
 * Strategy: each account is checked by email before insertion — completely
 * idempotent. Running the service multiple times will never duplicate data
 * or drop existing records.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        seedAdmin();
        seedSampleCustomer();
        log.info("[DataSeeder] Auth-service seeding complete.");
    }

    // -------------------------------------------------------------------------
    // Accounts
    // -------------------------------------------------------------------------

    private void seedAdmin() {
        String email = "admin@kinderland.vn";
        if (accountRepository.existsByEmail(email)) {
            log.info("[DataSeeder] Admin account already exists — skipping.");
            return;
        }
        Account admin = Account.builder()
                .username("admin")
                .email(email)
                .firstName("Admin")
                .lastName("Kinderland")
                .password(passwordEncoder.encode("Admin@123"))
                .role(Account.Role.ADMIN)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        accountRepository.save(admin);
        log.info("[DataSeeder] Created admin account: {}", email);
    }

    private void seedSampleCustomer() {
        String email = "customer@kinderland.vn";
        if (accountRepository.existsByEmail(email)) {
            log.info("[DataSeeder] Sample customer account already exists — skipping.");
            return;
        }
        Account customer = Account.builder()
                .username("customer01")
                .email(email)
                .firstName("Sample")
                .lastName("Customer")
                .password(passwordEncoder.encode("Customer@123"))
                .role(Account.Role.CUSTOMER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        accountRepository.save(customer);
        log.info("[DataSeeder] Created sample customer account: {}", email);
    }
}

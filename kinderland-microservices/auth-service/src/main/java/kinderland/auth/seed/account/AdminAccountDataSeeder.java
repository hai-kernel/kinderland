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
import java.util.List;
import java.util.Optional;

@Component
@Order(20)
@ConditionalOnProperty(name = "app.seed.account.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class AdminAccountDataSeeder extends AbstractDataSeeder {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String getName() {
        return "AdminAccountDataSeeder";
    }

    @Override
    @Transactional
    public void seed() {
        logStart();
        
        String defaultPassword = System.getenv("SEED_DEFAULT_PASSWORD");
        if (defaultPassword == null || defaultPassword.isEmpty()) {
            defaultPassword = "Seed@123456";
            log.warn("SEED_DEFAULT_PASSWORD environment variable not set. Using default development password.");
        }
        
        String encodedPassword = passwordEncoder.encode(defaultPassword);
        
        int created = 0;
        int skipped = 0;

        List<AdminSeedDef> admins = List.of(
                new AdminSeedDef("admin", "admin@kinderland.vn", "Admin", "Kinderland", Account.Role.ADMIN),
                new AdminSeedDef("manager", "manager@kinderland.vn", "Store", "Manager", Account.Role.MANAGER),
                new AdminSeedDef("product_admin", "product.admin@kinderland.vn", "Product", "Admin", Account.Role.MANAGER),
                new AdminSeedDef("marketing_admin", "marketing.admin@kinderland.vn", "Marketing", "Admin", Account.Role.MANAGER),
                new AdminSeedDef("support", "support@kinderland.vn", "Support", "Staff", Account.Role.MANAGER)
        );

        for (AdminSeedDef def : admins) {
            if (accountRepository.existsByEmail(def.email)) {
                log.info("Skipped admin account: {}", def.email);
                skipped++;
                continue;
            }

            Account account = Account.builder()
                    .username(def.username)
                    .email(def.email)
                    .firstName(def.firstName)
                    .lastName(def.lastName)
                    .password(encodedPassword)
                    .role(def.role)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            accountRepository.save(account);
            log.info("Created admin account: {}", def.email);
            created++;
        }

        logCompleted(created, skipped);
    }
    
    private record AdminSeedDef(String username, String email, String firstName, String lastName, Account.Role role) {}
}

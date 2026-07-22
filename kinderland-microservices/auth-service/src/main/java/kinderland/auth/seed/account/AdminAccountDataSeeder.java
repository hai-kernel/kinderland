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

@Component
@Order(20)
@ConditionalOnProperty(
        name = "app.seed.account.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class AdminAccountDataSeeder extends AbstractDataSeeder {

    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@123456";
    private static final String DEFAULT_MANAGER_PASSWORD = "Manager@123456";

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

        String adminPassword = getPasswordFromEnvironment(
                "SEED_ADMIN_PASSWORD",
                DEFAULT_ADMIN_PASSWORD
        );

        String managerPassword = getPasswordFromEnvironment(
                "SEED_MANAGER_PASSWORD",
                DEFAULT_MANAGER_PASSWORD
        );

        int created = 0;
        int skipped = 0;

        List<AdminSeedDef> accounts = List.of(
                new AdminSeedDef(
                        "admin",
                        "admin@kinderland.vn",
                        "Admin",
                        "Kinderland",
                        Account.Role.ADMIN
                ),
                new AdminSeedDef(
                        "manager",
                        "manager@kinderland.vn",
                        "Store",
                        "Manager",
                        Account.Role.MANAGER
                ),
                new AdminSeedDef(
                        "product_admin",
                        "product.admin@kinderland.vn",
                        "Product",
                        "Admin",
                        Account.Role.MANAGER
                ),
                new AdminSeedDef(
                        "marketing_admin",
                        "marketing.admin@kinderland.vn",
                        "Marketing",
                        "Admin",
                        Account.Role.MANAGER
                ),
                new AdminSeedDef(
                        "support",
                        "support@kinderland.vn",
                        "Support",
                        "Staff",
                        Account.Role.MANAGER
                )
        );

        for (AdminSeedDef accountDefinition : accounts) {
            if (accountRepository.existsByEmail(accountDefinition.email())) {
                log.info(
                        "Skipped seeded account because email already exists: {}",
                        accountDefinition.email()
                );

                skipped++;
                continue;
            }

            String rawPassword = getPasswordForRole(
                    accountDefinition.role(),
                    adminPassword,
                    managerPassword
            );

            Account account = buildAccount(
                    accountDefinition,
                    rawPassword
            );

            accountRepository.save(account);

            log.info(
                    "Created seeded account: email={}, role={}",
                    accountDefinition.email(),
                    accountDefinition.role()
            );

            created++;
        }

        logCompleted(created, skipped);
    }

    private Account buildAccount(
            AdminSeedDef definition,
            String rawPassword
    ) {
        LocalDateTime now = LocalDateTime.now();

        return Account.builder()
                .username(definition.username())
                .email(definition.email())
                .firstName(definition.firstName())
                .lastName(definition.lastName())
                .password(passwordEncoder.encode(rawPassword))
                .role(definition.role())
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private String getPasswordForRole(
            Account.Role role,
            String adminPassword,
            String managerPassword
    ) {
        return switch (role) {
            case ADMIN -> adminPassword;
            case MANAGER -> managerPassword;
            default -> throw new IllegalArgumentException(
                    "Unsupported seeded account role: " + role
            );
        };
    }

    private String getPasswordFromEnvironment(
            String environmentVariable,
            String developmentDefault
    ) {
        String password = System.getenv(environmentVariable);

        if (password == null || password.isBlank()) {
            log.warn(
                    "{} is not configured. Using development default password.",
                    environmentVariable
            );

            return developmentDefault;
        }

        return password;
    }

    private record AdminSeedDef(
            String username,
            String email,
            String firstName,
            String lastName,
            Account.Role role
    ) {
    }
}
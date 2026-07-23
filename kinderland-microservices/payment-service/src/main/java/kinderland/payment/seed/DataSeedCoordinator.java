package kinderland.payment.seed;

import kinderland.common.seed.DataSeeder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("dev")
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@Slf4j
public class DataSeedCoordinator implements ApplicationRunner {

    private final List<DataSeeder> seeders;

    public DataSeedCoordinator(List<DataSeeder> seeders) {
        this.seeders = seeders.stream()
                .sorted(AnnotationAwareOrderComparator.INSTANCE)
                .toList();
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting Kinderland Payment-Service seed process. Seeder count: {}", seeders.size());

        for (DataSeeder seeder : seeders) {
            if (!seeder.isEnabled()) {
                log.info("Skipping disabled seeder: {}", seeder.getName());
                continue;
            }
            try {
                log.info("Running seeder: {}", seeder.getName());
                seeder.seed();
                log.info("Seeder completed: {}", seeder.getName());
            } catch (Exception exception) {
                log.error("Seeder failed: {}", seeder.getName(), exception);
                throw exception;
            }
        }

        log.info("Kinderland Payment-Service seed process completed.");
    }
}

package kinderland.common.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDataSeeder implements DataSeeder {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected void logStart() {
        log.info("Starting seeder: {}", getName());
    }

    protected void logCompleted(int created, int skipped) {
        log.info("Completed seeder: {}. Created: {}, skipped: {}", getName(), created, skipped);
    }
}

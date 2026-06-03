package com.snowball.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Auto-runs safe, idempotent schema migrations on startup.
 * Each statement is wrapped in try/catch so failures are logged but don't block startup.
 */
@Component
public class DatabaseMigration implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMigration.class);
    private final JdbcTemplate jdbc;

    public DatabaseMigration(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) {
        safeRun("ALTER TABLE comments MODIFY COLUMN post_id BIGINT NULL",
                "comments.post_id → nullable");
        safeRun("ALTER TABLE comments MODIFY COLUMN source_id BIGINT NULL",
                "comments.source_id → nullable");
    }

    private void safeRun(String sql, String label) {
        try {
            jdbc.execute(sql);
            log.info("Migration OK: {}", label);
        } catch (Exception e) {
            log.debug("Migration skipped (already applied?): {} — {}", label, e.getMessage());
        }
    }
}

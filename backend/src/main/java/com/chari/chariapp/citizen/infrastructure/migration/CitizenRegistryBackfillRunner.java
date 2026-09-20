package com.chari.chariapp.citizen.infrastructure.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/** Runs before document migration so every imported document can resolve its protected citizen. */
@Configuration
public class CitizenRegistryBackfillRunner {
    private static final Logger log = LoggerFactory.getLogger(CitizenRegistryBackfillRunner.class);

    @Bean
    @ConditionalOnProperty(name = "app.citizens.backfill.enabled", havingValue = "true")
    @Order(100)
    ApplicationRunner citizenRegistryBackfillApplicationRunner(CitizenRegistryBackfillService service) {
        return arguments -> {
            CitizenRegistryBackfillService.BackfillReport report = service.backfillAll();
            log.info("Citizen registry backfill complete: sourceRecords={}, created={}, existing={}, skipped={}",
                    report.sourceRecords(), report.citizensCreated(), report.citizensAlreadyPresent(), report.recordsSkipped());
        };
    }
}

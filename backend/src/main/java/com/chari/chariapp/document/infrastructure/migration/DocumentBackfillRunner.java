package com.chari.chariapp.document.infrastructure.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/** Runs after citizen migration and copies missing documents into the encrypted source of truth. */
@Configuration
public class DocumentBackfillRunner {
    private static final Logger log = LoggerFactory.getLogger(DocumentBackfillRunner.class);

    @Bean
    @ConditionalOnProperty(name = "app.documents.backfill.enabled", havingValue = "true")
    @Order(110)
    ApplicationRunner documentBackfillApplicationRunner(DocumentBackfillService service) {
        return arguments -> {
            DocumentBackfillService.BackfillReport report = service.backfillAll();
            log.info("Document backfill complete: citizens={}, created={}, skipped={}",
                    report.citizensVisited(), report.documentsCreated(), report.documentsSkipped());
        };
    }
}

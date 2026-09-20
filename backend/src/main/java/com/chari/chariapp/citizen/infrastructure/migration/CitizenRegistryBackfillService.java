package com.chari.chariapp.citizen.infrastructure.migration;

import com.chari.chariapp.citizen.application.port.out.CitizenStore;
import com.chari.chariapp.citizen.domain.Citizen;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.citizen.domain.NationalIdReference;
import com.chari.chariapp.repository.NormalizedCitizenDocumentRepository;
import com.chari.chariapp.shared.security.PersonalDataNormalizer;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

/**
 * Idempotent migration from normalized person records to the protected citizen registry.
 * The imported data does not contain phone numbers, so this deliberately creates no verified phone.
 */
@Service
public class CitizenRegistryBackfillService {

    private final NormalizedCitizenDocumentRepository normalizedDocuments;
    private final CitizenStore citizenStore;
    private final PersonalDataProtector dataProtector;
    private final Clock clock;

    public CitizenRegistryBackfillService(
            NormalizedCitizenDocumentRepository normalizedDocuments,
            CitizenStore citizenStore,
            PersonalDataProtector dataProtector,
            Clock clock
    ) {
        this.normalizedDocuments = normalizedDocuments;
        this.citizenStore = citizenStore;
        this.dataProtector = dataProtector;
        this.clock = clock;
    }

    @Transactional
    public BackfillReport backfillAll() {
        int sourceRecords = 0;
        int created = 0;
        int existing = 0;
        int skipped = 0;

        for (String sourceNationalId : normalizedDocuments.nationalIds()) {
            sourceRecords++;
            String legacyNationalId = sourceNationalId;
            if (legacyNationalId == null || legacyNationalId.isBlank()) {
                skipped++;
                continue;
            }

            String lookup = dataProtector.lookup(PersonalDataNormalizer.nationalId(legacyNationalId));
            if (citizenStore.existsByNationalIdLookup(lookup)) {
                existing++;
                continue;
            }

            citizenStore.save(new Citizen(
                    CitizenId.newId(),
                    new NationalIdReference(lookup, dataProtector.encrypt(legacyNationalId)),
                    null,
                    null,
                    Instant.now(clock)
            ));
            created++;
        }
        return new BackfillReport(sourceRecords, created, existing, skipped);
    }

    public record BackfillReport(int sourceRecords, int citizensCreated, int citizensAlreadyPresent, int recordsSkipped) {
    }
}

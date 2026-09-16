package com.chari.chariapp.shared.infrastructure.development;

import com.chari.chariapp.citizen.application.port.out.CitizenStore;
import com.chari.chariapp.citizen.domain.Citizen;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.citizen.domain.NationalIdReference;
import com.chari.chariapp.citizen.domain.PhoneReference;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

/**
 * Opt-in hosted test fixture for the enrollment flow.
 *
 * <p>It creates only a verified citizen-registry record. It never creates an
 * account or grants a role, and is disabled unless explicitly enabled through
 * {@code APP_TEST_DATA_ENROLLMENT_CITIZEN_ENABLED=true}.</p>
 */
@Component
@ConditionalOnProperty(prefix = "app.test-data.enrollment-citizen", name = "enabled", havingValue = "true")
public class EnrollmentTestCitizenInitializer implements ApplicationRunner {

    public static final String NATIONAL_ID = "CID001";
    public static final String PHONE = "+23590000003";

    private static final Logger log = LoggerFactory.getLogger(EnrollmentTestCitizenInitializer.class);

    private final CitizenStore citizenStore;
    private final PersonalDataProtector dataProtector;
    private final Clock clock;

    public EnrollmentTestCitizenInitializer(
            CitizenStore citizenStore,
            PersonalDataProtector dataProtector,
            Clock clock
    ) {
        this.citizenStore = citizenStore;
        this.dataProtector = dataProtector;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        String nationalIdLookup = dataProtector.lookup(NATIONAL_ID);
        boolean created = citizenStore.findByNationalIdLookup(nationalIdLookup).isEmpty();

        if (created) {
            Instant now = Instant.now(clock);
            citizenStore.save(new Citizen(
                    CitizenId.newId(),
                    new NationalIdReference(nationalIdLookup, dataProtector.encrypt(NATIONAL_ID)),
                    new PhoneReference(dataProtector.lookup(PHONE), dataProtector.encrypt(PHONE)),
                    now,
                    now
            ));
        }

        log.warn("Hosted enrollment test citizen {}", created ? "created" : "already exists");
    }
}

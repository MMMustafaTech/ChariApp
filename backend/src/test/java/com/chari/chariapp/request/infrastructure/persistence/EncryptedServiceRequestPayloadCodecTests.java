package com.chari.chariapp.request.infrastructure.persistence;

import com.chari.chariapp.request.domain.*;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class EncryptedServiceRequestPayloadCodecTests {

    @Test
    void encryptsAndRestoresSubmissionDetails() {
        EncryptedServiceRequestPayloadCodec codec =
                new EncryptedServiceRequestPayloadCodec(new ReversibleTestProtector());
        ServiceRequestSubmissionDetails original = new ServiceRequestSubmissionDetails(
                RequestBeneficiaryType.SELF, null, "PAY-2026-1", null);

        String encrypted = codec.encrypt(original);

        assertThat(encrypted).doesNotContain("PAY-2026-1");
        assertThat(codec.decrypt(encrypted)).isEqualTo(original);
    }

    @Test
    void ignoresWitnessesStoredByThePreviousPayloadVersion() {
        ReversibleTestProtector protector = new ReversibleTestProtector();
        EncryptedServiceRequestPayloadCodec codec = new EncryptedServiceRequestPayloadCodec(protector);
        String previousJson = """
                {
                  "beneficiaryType":"SELF",
                  "dependentBirthCertificateId":null,
                  "witnesses":[
                    {"fullName":"Legacy Witness","nationalId":"CID101","relationship":"BIOLOGICAL_PARENT"}
                  ],
                  "paymentReference":"PAY-OLD",
                  "lossReportNumber":null
                }
                """;

        ServiceRequestSubmissionDetails details = codec.decrypt(protector.encrypt(previousJson));

        assertThat(details.paymentReference()).isEqualTo("PAY-OLD");
        assertThat(details.beneficiaryType()).isEqualTo(RequestBeneficiaryType.SELF);
    }

    private static final class ReversibleTestProtector implements PersonalDataProtector {
        public String lookup(String normalizedValue) { return normalizedValue; }
        public String encrypt(String plaintext) {
            return Base64.getEncoder().encodeToString(plaintext.getBytes(StandardCharsets.UTF_8));
        }
        public String decrypt(String ciphertext) {
            return new String(Base64.getDecoder().decode(ciphertext), StandardCharsets.UTF_8);
        }
    }
}

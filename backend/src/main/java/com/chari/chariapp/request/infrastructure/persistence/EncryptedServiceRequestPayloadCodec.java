package com.chari.chariapp.request.infrastructure.persistence;

import com.chari.chariapp.request.domain.ServiceRequestSubmissionDetails;
import com.chari.chariapp.shared.security.PersonalDataProtector;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class EncryptedServiceRequestPayloadCodec {
    private final PersonalDataProtector protector;
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    public EncryptedServiceRequestPayloadCodec(PersonalDataProtector protector) {
        this.protector = protector;
    }

    public String encrypt(ServiceRequestSubmissionDetails details) {
        if (details == null) return null;
        try {
            return protector.encrypt(mapper.writeValueAsString(details));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to serialize service request details", exception);
        }
    }

    public ServiceRequestSubmissionDetails decrypt(String payload) {
        if (payload == null || payload.isBlank()) return null;
        try {
            return mapper.readValue(protector.decrypt(payload), ServiceRequestSubmissionDetails.class);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to read service request details", exception);
        }
    }
}

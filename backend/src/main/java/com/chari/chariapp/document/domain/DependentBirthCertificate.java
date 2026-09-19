package com.chari.chariapp.document.domain;

import com.chari.chariapp.citizen.domain.CitizenId;

import java.time.Instant;
import java.util.UUID;

public record DependentBirthCertificate(
        UUID id,
        UUID requestId,
        CitizenId parentCitizenId,
        MyBirthCertificate certificate,
        Instant createdAt
) {
}

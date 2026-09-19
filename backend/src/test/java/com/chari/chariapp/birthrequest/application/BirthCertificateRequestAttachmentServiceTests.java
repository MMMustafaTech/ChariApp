package com.chari.chariapp.birthrequest.application;

import com.chari.chariapp.account.application.port.out.AccountStore;
import com.chari.chariapp.account.domain.Account;
import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.account.domain.AccountRole;
import com.chari.chariapp.account.domain.AccountStatus;
import com.chari.chariapp.account.domain.EmailReference;
import com.chari.chariapp.birthrequest.application.port.out.BirthCertificateRequestAttachmentStore;
import com.chari.chariapp.birthrequest.application.port.out.BirthCertificateRequestStore;
import com.chari.chariapp.birthrequest.domain.BirthCertificateRequest;
import com.chari.chariapp.birthrequest.domain.BirthCertificateRequestAttachment;
import com.chari.chariapp.birthrequest.domain.BirthCertificateRequestKind;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.request.application.AttachmentUpload;
import com.chari.chariapp.request.application.PassportRequestActorAccess;
import com.chari.chariapp.request.domain.AttachmentDocumentType;
import com.chari.chariapp.request.application.port.out.AttachmentContentStore;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import java.io.ByteArrayInputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BirthCertificateRequestAttachmentServiceTests {
    private static final Instant NOW = Instant.parse("2026-08-29T00:00:00Z");

    @Test
    void storesValidatedPdfForTheCitizenOwnedBirthRequest() {
        CitizenId citizenId = CitizenId.newId(); AccountId citizenAccountId = AccountId.newId();
        BirthCertificateRequest request = BirthCertificateRequest.submitted(citizenId, BirthCertificateRequestKind.NEWBORN_REGISTRATION, null, NOW.minusSeconds(60));
        AccountStore accounts = mock(AccountStore.class); BirthCertificateRequestStore requests = mock(BirthCertificateRequestStore.class);
        BirthCertificateRequestAttachmentStore attachments = mock(BirthCertificateRequestAttachmentStore.class); AttachmentContentStore content = mock(AttachmentContentStore.class); OperationalAuditStore audit = mock(OperationalAuditStore.class);
        when(accounts.findById(citizenAccountId)).thenReturn(Optional.of(citizen(citizenAccountId, citizenId)));
        when(requests.findById(request.id())).thenReturn(Optional.of(request));
        when(attachments.save(any(BirthCertificateRequestAttachment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        BirthCertificateRequestAttachmentService service = new BirthCertificateRequestAttachmentService(new PassportRequestActorAccess(accounts), requests, attachments, content, audit, Clock.fixed(NOW, ZoneOffset.UTC));
        byte[] pdf = new byte[]{'%', 'P', 'D', 'F', '-', '1'};
        BirthCertificateRequestAttachment saved = service.upload(citizenAccountId, request.id(), AttachmentDocumentType.BIRTH_CERTIFICATE_COPY, new AttachmentUpload("birth-report.pdf", "application/pdf", pdf.length, new ByteArrayInputStream(pdf)));
        assertThat(saved.contentType()).isEqualTo("application/pdf");
        verify(content).store(eq(saved.storageKey()), any());
        verify(audit).record(citizenAccountId.value().toString(), "BIRTH_CERTIFICATE_REQUEST_ATTACHMENT_UPLOADED", "SERVICE_REQUEST", request.id().toString(), null, NOW);
    }

    private Account citizen(AccountId accountId, CitizenId citizenId) {
        return new Account(accountId, citizenId, new EmailReference("a".repeat(64), "citizen-ciphertext"), "bcrypt-hash", AccountStatus.ACTIVE, java.util.Set.of(AccountRole.CITIZEN), NOW);
    }
}

package com.chari.chariapp.request.application;

import com.chari.chariapp.account.application.port.out.AccountStore;
import com.chari.chariapp.account.domain.Account;
import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.account.domain.AccountRole;
import com.chari.chariapp.account.domain.AccountStatus;
import com.chari.chariapp.account.domain.EmailReference;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.request.application.port.out.AttachmentContentStore;
import com.chari.chariapp.request.application.port.out.PassportRequestAttachmentStore;
import com.chari.chariapp.request.application.port.out.PassportRequestStore;
import com.chari.chariapp.request.domain.PassportRequest;
import com.chari.chariapp.request.domain.PassportRequestAttachment;
import com.chari.chariapp.request.domain.AttachmentDocumentType;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PassportRequestAttachmentServiceTests {

    private static final Instant NOW = Instant.parse("2026-08-29T00:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Test
    void storesValidatedPngAndRecordsMetadataWithoutExposingTheFilePath() {
        CitizenId citizenId = CitizenId.newId();
        AccountId citizenAccountId = AccountId.newId();
        PassportRequest request = PassportRequest.submitted(citizenId, NOW.minusSeconds(60));

        AccountStore accounts = mock(AccountStore.class);
        PassportRequestStore requests = mock(PassportRequestStore.class);
        PassportRequestAttachmentStore attachments = mock(PassportRequestAttachmentStore.class);
        AttachmentContentStore content = mock(AttachmentContentStore.class);
        OperationalAuditStore audit = mock(OperationalAuditStore.class);
        when(accounts.findById(citizenAccountId)).thenReturn(Optional.of(citizen(citizenAccountId, citizenId)));
        when(requests.findById(request.id())).thenReturn(Optional.of(request));
        when(attachments.save(any(PassportRequestAttachment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PassportRequestAttachmentService service = new PassportRequestAttachmentService(
                new PassportRequestActorAccess(accounts), requests, attachments, content, audit, CLOCK
        );

        byte[] png = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00};
        PassportRequestAttachment saved = service.upload(citizenAccountId, request.id(), AttachmentDocumentType.PERSONAL_PHOTO,
                new AttachmentUpload("../passport-photo.png", "image/png", png.length, new ByteArrayInputStream(png)));

        assertThat(saved.originalFileName()).isEqualTo("passport-photo.png");
        assertThat(saved.contentType()).isEqualTo("image/png");
        assertThat(saved.uploadedAt()).isEqualTo(NOW);
        verify(content).store(eq(saved.storageKey()), any());
        verify(audit).record(citizenAccountId.value().toString(), "PASSPORT_REQUEST_ATTACHMENT_UPLOADED",
                "SERVICE_REQUEST", request.id().toString(), null, NOW);
    }

    @Test
    void rejectsAFileWhoseContentDoesNotMatchItsClaimedType() {
        CitizenId citizenId = CitizenId.newId();
        AccountId citizenAccountId = AccountId.newId();
        PassportRequest request = PassportRequest.submitted(citizenId, NOW.minusSeconds(60));

        AccountStore accounts = mock(AccountStore.class);
        PassportRequestStore requests = mock(PassportRequestStore.class);
        PassportRequestAttachmentStore attachments = mock(PassportRequestAttachmentStore.class);
        AttachmentContentStore content = mock(AttachmentContentStore.class);
        OperationalAuditStore audit = mock(OperationalAuditStore.class);
        when(accounts.findById(citizenAccountId)).thenReturn(Optional.of(citizen(citizenAccountId, citizenId)));
        when(requests.findById(request.id())).thenReturn(Optional.of(request));

        PassportRequestAttachmentService service = new PassportRequestAttachmentService(
                new PassportRequestActorAccess(accounts), requests, attachments, content, audit, CLOCK
        );

        byte[] executableLookingData = new byte[]{0x4D, 0x5A, 0x00, 0x00, 0x00};
        assertThatThrownBy(() -> service.upload(citizenAccountId, request.id(), AttachmentDocumentType.PERSONAL_PHOTO,
                new AttachmentUpload("photo.png", "image/png", executableLookingData.length,
                        new ByteArrayInputStream(executableLookingData))))
                .isInstanceOf(AttachmentUploadException.class)
                .hasMessage("Attachment content does not match its declared type");

        verify(content, never()).store(any(), any());
        verify(attachments, never()).save(any());
        verify(audit, never()).record(any(), any(), any(), any(), any(), any());
    }

    private Account citizen(AccountId accountId, CitizenId citizenId) {
        return new Account(accountId, citizenId, new EmailReference("a".repeat(64), "citizen-ciphertext"),
                "bcrypt-hash", AccountStatus.ACTIVE, java.util.Set.of(AccountRole.CITIZEN), NOW);
    }
}

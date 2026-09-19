package com.chari.chariapp.identityrequest.infrastructure;

import com.chari.chariapp.identityrequest.application.NationalIdentityRequestQueryService;
import com.chari.chariapp.additionaldocument.application.port.out.AdditionalDocumentRequestStore;
import com.chari.chariapp.identityrequest.application.NationalIdentityRequestAttachmentService;
import com.chari.chariapp.identityrequest.application.ReviewNationalIdentityRequestService;
import com.chari.chariapp.identityrequest.application.SubmitNationalIdentityRequestService;
import com.chari.chariapp.identityrequest.application.port.out.NationalIdentityRequestStatusHistoryStore;
import com.chari.chariapp.identityrequest.application.port.out.NationalIdentityRequestAttachmentStore;
import com.chari.chariapp.identityrequest.application.port.out.NationalIdentityRequestStore;
import com.chari.chariapp.request.application.PassportRequestActorAccess;
import com.chari.chariapp.request.application.port.out.AttachmentContentStore;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import com.chari.chariapp.notification.application.NotificationService;
import com.chari.chariapp.document.application.port.out.CitizenDocumentReadStore;
import com.chari.chariapp.document.application.DocumentIssuanceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;

@Configuration
public class NationalIdentityRequestConfiguration {
    @Bean SubmitNationalIdentityRequestService submitNationalIdentityRequestService(PassportRequestActorAccess access, NationalIdentityRequestStore requests, NationalIdentityRequestStatusHistoryStore history, CitizenDocumentReadStore documents, OperationalAuditStore audit, Clock clock) { return new SubmitNationalIdentityRequestService(access, requests, history, documents, audit, clock); }
    @Bean ReviewNationalIdentityRequestService reviewNationalIdentityRequestService(PassportRequestActorAccess access, NationalIdentityRequestStore requests, NationalIdentityRequestStatusHistoryStore history, OperationalAuditStore audit, NotificationService notifications, AdditionalDocumentRequestStore additionalDocuments, DocumentIssuanceService documentIssuance, NationalIdentityRequestAttachmentService attachments, Clock clock) { return new ReviewNationalIdentityRequestService(access, requests, history, audit, notifications, additionalDocuments, documentIssuance, attachments, clock); }
    @Bean NationalIdentityRequestQueryService nationalIdentityRequestQueryService(PassportRequestActorAccess access, NationalIdentityRequestStore requests, NationalIdentityRequestStatusHistoryStore history) { return new NationalIdentityRequestQueryService(access, requests, history); }
    @Bean NationalIdentityRequestAttachmentService nationalIdentityRequestAttachmentService(PassportRequestActorAccess access, NationalIdentityRequestStore requests, NationalIdentityRequestAttachmentStore attachments, AttachmentContentStore contentStore, OperationalAuditStore audit, Clock clock) { return new NationalIdentityRequestAttachmentService(access, requests, attachments, contentStore, audit, clock); }
}

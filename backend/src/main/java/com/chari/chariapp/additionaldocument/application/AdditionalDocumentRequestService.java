package com.chari.chariapp.additionaldocument.application;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.additionaldocument.application.port.out.AdditionalDocumentAttachmentStore;
import com.chari.chariapp.additionaldocument.application.port.out.AdditionalDocumentRequestStore;
import com.chari.chariapp.additionaldocument.domain.*;
import com.chari.chariapp.birthrequest.application.port.out.BirthCertificateRequestStore;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.exception.NotFoundException;
import com.chari.chariapp.identityrequest.application.port.out.NationalIdentityRequestStore;
import com.chari.chariapp.notification.application.NotificationService;
import com.chari.chariapp.notification.domain.NotificationType;
import com.chari.chariapp.operations.domain.ServiceRequestType;
import com.chari.chariapp.request.application.*;
import com.chari.chariapp.request.application.port.out.AttachmentContentStore;
import com.chari.chariapp.request.application.port.out.PassportRequestStore;
import com.chari.chariapp.shared.application.port.out.OperationalAuditStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdditionalDocumentRequestService {
    private final PassportRequestActorAccess access;
    private final PassportRequestStore passports;
    private final NationalIdentityRequestStore identities;
    private final BirthCertificateRequestStore births;
    private final AdditionalDocumentRequestStore requests;
    private final AdditionalDocumentAttachmentStore attachments;
    private final AttachmentContentStore contentStore;
    private final NotificationService notifications;
    private final OperationalAuditStore audit;
    private final Clock clock;

    public AdditionalDocumentRequestService(PassportRequestActorAccess access, PassportRequestStore passports,
                                            NationalIdentityRequestStore identities, BirthCertificateRequestStore births,
                                            AdditionalDocumentRequestStore requests,
                                            AdditionalDocumentAttachmentStore attachments,
                                            AttachmentContentStore contentStore, NotificationService notifications,
                                            OperationalAuditStore audit, Clock clock) {
        this.access=access; this.passports=passports; this.identities=identities; this.births=births;
        this.requests=requests; this.attachments=attachments; this.contentStore=contentStore;
        this.notifications=notifications; this.audit=audit; this.clock=clock;
    }

    @Transactional
    public AdditionalDocumentRequest request(AccountId actorId, ServiceRequestType type, UUID requestId,
                                             String message, Set<String> requiredDocuments) {
        access.requireActiveOperator(actorId);
        OriginalRequest original = original(type, requestId);
        if (original.status() != OriginalStatus.UNDER_REVIEW || !actorId.equals(original.reviewedBy())) {
            throw new AdditionalDocumentRequestConflictException("ADDITIONAL_DOCUMENTS_REVIEWER_MISMATCH");
        }
        if (requests.hasOpenRequest(type, requestId)) {
            throw new AdditionalDocumentRequestConflictException("ADDITIONAL_DOCUMENTS_ALREADY_REQUESTED");
        }
        Instant now=Instant.now(clock);
        AdditionalDocumentRequest saved=requests.save(AdditionalDocumentRequest.create(type,requestId,
                original.citizenId(),message,requiredDocuments,actorId,now));
        notifications.publish(original.citizenId(), NotificationType.ADDITIONAL_DOCUMENTS_REQUESTED,
                "Additional documents requested", "Additional documents are required to continue reviewing your request.");
        audit.record(actorId.value().toString(),"ADDITIONAL_DOCUMENTS_REQUESTED","SERVICE_REQUEST",requestId.toString(),null,now);
        return saved;
    }

    @Transactional(readOnly=true)
    public List<AdditionalDocumentRequest> mine(AccountId actorId) {
        return requests.findByCitizenId(access.requireActiveCitizen(actorId));
    }

    @Transactional(readOnly=true)
    public List<AdditionalDocumentRequest> forOriginalRequest(AccountId actorId,ServiceRequestType type,UUID requestId) {
        access.requireActiveOperator(actorId); original(type,requestId); return requests.findByOriginalRequest(type,requestId);
    }

    @Transactional
    public AdditionalDocumentAttachment upload(AccountId actorId,UUID additionalRequestId,String documentName,AttachmentUpload upload) {
        CitizenId citizenId=access.requireActiveCitizen(actorId);
        AdditionalDocumentRequest request=owned(citizenId,additionalRequestId);
        if(request.status()!=AdditionalDocumentRequestStatus.REQUESTED) throw new AdditionalDocumentRequestConflictException("ADDITIONAL_DOCUMENTS_UPLOAD_CLOSED");
        String normalized=documentName==null?"":documentName.trim();
        if(!request.requiredDocuments().contains(normalized)) throw new IllegalArgumentException("Document name was not requested");
        String contentType=AttachmentUploadPolicy.validateMetadata(upload); String storageKey=UUID.randomUUID().toString(); Instant now=Instant.now(clock);
        AdditionalDocumentAttachment attachment=new AdditionalDocumentAttachment(UUID.randomUUID(),request.id(),normalized,storageKey,
                AttachmentUploadPolicy.safeFileName(upload.originalFileName()),contentType,upload.sizeBytes(),actorId,now);
        try(BufferedInputStream content=new BufferedInputStream(upload.content())){AttachmentUploadPolicy.validateSignature(content,contentType);contentStore.store(storageKey,content);}
        catch(IOException ex){throw new UncheckedIOException("Unable to read attachment",ex);}
        try {AdditionalDocumentAttachment saved=attachments.save(attachment);audit.record(actorId.value().toString(),"ADDITIONAL_DOCUMENT_UPLOADED","ADDITIONAL_DOCUMENT_REQUEST",request.id().toString(),null,now);return saved;}
        catch(RuntimeException ex){contentStore.delete(storageKey);throw ex;}
    }

    @Transactional
    public AdditionalDocumentRequest submit(AccountId actorId,UUID additionalRequestId) {
        CitizenId citizenId=access.requireActiveCitizen(actorId); AdditionalDocumentRequest request=owned(citizenId,additionalRequestId);
        Set<String> uploaded=attachments.findByAdditionalRequestId(request.id()).stream().map(AdditionalDocumentAttachment::documentName).collect(Collectors.toSet());
        if(!uploaded.containsAll(request.requiredDocuments())) throw new AdditionalDocumentRequestConflictException("ADDITIONAL_DOCUMENTS_INCOMPLETE");
        Instant now=Instant.now(clock); AdditionalDocumentRequest saved=requests.save(request.submit(now));
        audit.record(actorId.value().toString(),"ADDITIONAL_DOCUMENTS_SUBMITTED","SERVICE_REQUEST",request.requestId().toString(),null,now);
        return saved;
    }

    @Transactional
    public AdditionalDocumentRequest resolve(AccountId actorId,UUID additionalRequestId) {
        access.requireActiveOperator(actorId); AdditionalDocumentRequest request=require(additionalRequestId);
        OriginalRequest original=original(request.serviceType(),request.requestId());
        if(original.status()!=OriginalStatus.UNDER_REVIEW || !actorId.equals(original.reviewedBy())) {
            throw new AdditionalDocumentRequestConflictException("ADDITIONAL_DOCUMENTS_REVIEWER_MISMATCH");
        }
        Instant now=Instant.now(clock); AdditionalDocumentRequest saved=requests.save(request.resolve(actorId,now));
        notifications.publish(request.citizenId(),NotificationType.ADDITIONAL_DOCUMENTS_ACCEPTED,
                "Additional documents accepted","The additional documents for your request were accepted.");
        audit.record(actorId.value().toString(),"ADDITIONAL_DOCUMENTS_RESOLVED","SERVICE_REQUEST",request.requestId().toString(),null,now);
        return saved;
    }

    @Transactional(readOnly=true)
    public List<AdditionalDocumentAttachment> myAttachments(AccountId actorId,UUID requestId){owned(access.requireActiveCitizen(actorId),requestId);return attachments.findByAdditionalRequestId(requestId);}
    @Transactional(readOnly=true)
    public List<AdditionalDocumentAttachment> operationalAttachments(AccountId actorId,UUID requestId){access.requireActiveOperator(actorId);require(requestId);return attachments.findByAdditionalRequestId(requestId);}
    @Transactional(readOnly=true)
    public AdditionalDocumentAttachmentContent myContent(AccountId actorId,UUID requestId,UUID attachmentId){owned(access.requireActiveCitizen(actorId),requestId);return content(requestId,attachmentId);}
    @Transactional(readOnly=true)
    public AdditionalDocumentAttachmentContent operationalContent(AccountId actorId,UUID requestId,UUID attachmentId){access.requireActiveOperator(actorId);require(requestId);return content(requestId,attachmentId);}

    public boolean hasUnresolved(ServiceRequestType type,UUID requestId){return requests.hasOpenRequest(type,requestId);}

    private AdditionalDocumentRequest owned(CitizenId citizenId,UUID id){AdditionalDocumentRequest request=require(id);if(!request.citizenId().equals(citizenId))throw new NotFoundException("Additional document request not found");return request;}
    private AdditionalDocumentRequest require(UUID id){return requests.findById(id).orElseThrow(()->new NotFoundException("Additional document request not found"));}
    private AdditionalDocumentAttachmentContent content(UUID requestId,UUID attachmentId){AdditionalDocumentAttachment a=attachments.findByIdAndAdditionalRequestId(attachmentId,requestId).orElseThrow(()->new NotFoundException("Attachment not found"));return new AdditionalDocumentAttachmentContent(a,contentStore.read(a.storageKey()));}

    private OriginalRequest original(ServiceRequestType type,UUID id){return switch(type){
        case PASSPORT -> passports.findById(id).map(r->new OriginalRequest(r.citizenId(),OriginalStatus.valueOf(r.status().name()),r.reviewedBy())).orElseThrow(()->new NotFoundException("Request not found"));
        case NATIONAL_IDENTITY -> identities.findById(id).map(r->new OriginalRequest(r.citizenId(),OriginalStatus.valueOf(r.status().name()),r.reviewedBy())).orElseThrow(()->new NotFoundException("Request not found"));
        case BIRTH_CERTIFICATE -> births.findById(id).map(r->new OriginalRequest(r.citizenId(),OriginalStatus.valueOf(r.status().name()),r.reviewedBy())).orElseThrow(()->new NotFoundException("Request not found"));};}
    private enum OriginalStatus{SUBMITTED,UNDER_REVIEW,APPROVED,REJECTED}
    private record OriginalRequest(CitizenId citizenId,OriginalStatus status,AccountId reviewedBy){}
}

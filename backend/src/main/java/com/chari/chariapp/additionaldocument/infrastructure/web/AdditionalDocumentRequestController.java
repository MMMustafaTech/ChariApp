package com.chari.chariapp.additionaldocument.infrastructure.web;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.additionaldocument.application.AdditionalDocumentAttachmentContent;
import com.chari.chariapp.additionaldocument.application.AdditionalDocumentRequestService;
import com.chari.chariapp.additionaldocument.domain.AdditionalDocumentAttachment;
import com.chari.chariapp.additionaldocument.domain.AdditionalDocumentRequest;
import com.chari.chariapp.operations.domain.ServiceRequestType;
import com.chari.chariapp.request.application.AttachmentUpload;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
public class AdditionalDocumentRequestController {
    private final AdditionalDocumentRequestService service;
    public AdditionalDocumentRequestController(AdditionalDocumentRequestService service){this.service=service;}

    @PostMapping("/api/v1/operations/requests/{serviceType}/{requestId}/additional-documents")
    @PreAuthorize("hasAuthority('PERM_REQUEST_ADDITIONAL_DOCUMENTS')")
    @ResponseStatus(HttpStatus.CREATED)
    public AdditionalDocumentRequest request(@AuthenticationPrincipal Jwt jwt,@PathVariable ServiceRequestType serviceType,
                                             @PathVariable UUID requestId,@Valid @RequestBody AdditionalDocumentsBody body){
        return service.request(actor(jwt),serviceType,requestId,body.message(),body.requiredDocuments());
    }

    @GetMapping("/api/v1/operations/requests/{serviceType}/{requestId}/additional-documents")
    @PreAuthorize("hasAuthority('PERM_REQUEST_VIEW')")
    public List<AdditionalDocumentRequest> forRequest(@AuthenticationPrincipal Jwt jwt,@PathVariable ServiceRequestType serviceType,@PathVariable UUID requestId){return service.forOriginalRequest(actor(jwt),serviceType,requestId);}

    @GetMapping("/api/v1/me/additional-document-requests")
    public List<AdditionalDocumentRequest> mine(@AuthenticationPrincipal Jwt jwt){return service.mine(actor(jwt));}

    @PostMapping(value="/api/v1/me/additional-document-requests/{additionalRequestId}/attachments",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AttachmentResponse upload(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID additionalRequestId,
                                     @RequestParam String documentName,@RequestPart("file") MultipartFile file){
        try{return AttachmentResponse.from(service.upload(actor(jwt),additionalRequestId,documentName,
                new AttachmentUpload(file.getOriginalFilename(),file.getContentType(),file.getSize(),file.getInputStream())));}
        catch(IOException ex){throw new IllegalArgumentException("Unable to read attachment",ex);}
    }

    @PostMapping("/api/v1/me/additional-document-requests/{additionalRequestId}/submit")
    public AdditionalDocumentRequest submit(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID additionalRequestId){return service.submit(actor(jwt),additionalRequestId);}

    @PostMapping("/api/v1/operations/additional-document-requests/{additionalRequestId}/resolve")
    @PreAuthorize("hasAuthority('PERM_REQUEST_ADDITIONAL_DOCUMENTS')")
    public AdditionalDocumentRequest resolve(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID additionalRequestId){return service.resolve(actor(jwt),additionalRequestId);}

    @GetMapping("/api/v1/me/additional-document-requests/{additionalRequestId}/attachments")
    public List<AttachmentResponse> myAttachments(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID additionalRequestId){return service.myAttachments(actor(jwt),additionalRequestId).stream().map(AttachmentResponse::from).toList();}
    @GetMapping("/api/v1/operations/additional-document-requests/{additionalRequestId}/attachments")
    @PreAuthorize("hasAuthority('PERM_REQUEST_VIEW')")
    public List<AttachmentResponse> operationalAttachments(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID additionalRequestId){return service.operationalAttachments(actor(jwt),additionalRequestId).stream().map(AttachmentResponse::from).toList();}
    @GetMapping("/api/v1/me/additional-document-requests/{additionalRequestId}/attachments/{attachmentId}/content")
    public ResponseEntity<InputStreamResource> myContent(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID additionalRequestId,@PathVariable UUID attachmentId){return content(service.myContent(actor(jwt),additionalRequestId,attachmentId));}
    @GetMapping("/api/v1/operations/additional-document-requests/{additionalRequestId}/attachments/{attachmentId}/content")
    @PreAuthorize("hasAuthority('PERM_REQUEST_VIEW')")
    public ResponseEntity<InputStreamResource> operationalContent(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID additionalRequestId,@PathVariable UUID attachmentId){return content(service.operationalContent(actor(jwt),additionalRequestId,attachmentId));}

    private static ResponseEntity<InputStreamResource> content(AdditionalDocumentAttachmentContent content){var a=content.attachment();return ResponseEntity.ok().contentType(MediaType.parseMediaType(a.contentType())).contentLength(a.sizeBytes()).header("X-Content-Type-Options","nosniff").header("Cache-Control","private, no-store").header("Content-Disposition",ContentDisposition.attachment().filename(a.originalFileName()).build().toString()).body(new InputStreamResource(content.content()));}
    private static AccountId actor(Jwt jwt){return new AccountId(UUID.fromString(jwt.getSubject()));}
    public record AdditionalDocumentsBody(@Size(max=1000) String message,@NotEmpty @Size(max=20) Set<@Size(min=1,max=160) String> requiredDocuments){}
    public record AttachmentResponse(UUID id,String documentName,String fileName,String contentType,long sizeBytes,java.time.Instant uploadedAt){static AttachmentResponse from(AdditionalDocumentAttachment a){return new AttachmentResponse(a.id(),a.documentName(),a.originalFileName(),a.contentType(),a.sizeBytes(),a.uploadedAt());}}
}

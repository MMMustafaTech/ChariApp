package com.chari.chariapp.identityrequest.infrastructure.web;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.identityrequest.application.NationalIdentityRequestQueryService;
import com.chari.chariapp.identityrequest.application.NationalIdentityRequestAttachmentService;
import com.chari.chariapp.identityrequest.application.NationalIdentityAttachmentContent;
import com.chari.chariapp.identityrequest.application.ReviewNationalIdentityRequestService;
import com.chari.chariapp.identityrequest.application.SubmitNationalIdentityRequestService;
import com.chari.chariapp.identityrequest.domain.NationalIdentityRequest;
import com.chari.chariapp.identityrequest.domain.NationalIdentityRequestKind;
import com.chari.chariapp.identityrequest.domain.NationalIdentityRequestStatus;
import com.chari.chariapp.identityrequest.domain.NationalIdentityRequestStatusChange;
import com.chari.chariapp.identityrequest.domain.NationalIdentityRequestAttachment;
import com.chari.chariapp.request.application.AttachmentUpload;
import com.chari.chariapp.request.domain.RequestBeneficiaryType;
import com.chari.chariapp.request.domain.ServiceRequestSubmissionDetails;
import com.chari.chariapp.request.domain.AttachmentDocumentType;
import com.chari.chariapp.request.application.AttachmentRequirements;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.InputStreamResource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
public class NationalIdentityRequestController {
    private final SubmitNationalIdentityRequestService submitService;
    private final ReviewNationalIdentityRequestService reviewService;
    private final NationalIdentityRequestQueryService queryService;
    private final NationalIdentityRequestAttachmentService attachmentService;

    public NationalIdentityRequestController(SubmitNationalIdentityRequestService submitService,
                                             ReviewNationalIdentityRequestService reviewService,
                                             NationalIdentityRequestQueryService queryService,
                                             NationalIdentityRequestAttachmentService attachmentService) {
        this.submitService = submitService; this.reviewService = reviewService; this.queryService = queryService; this.attachmentService = attachmentService;
    }

    @PostMapping("/api/v1/me/national-identity-requests")
    ResponseEntity<NationalIdentityRequest> submit(@Valid @RequestBody Submission body, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(submitService.submit(accountId(jwt), body.kind(), body.reason(), body.toDetails()));
    }

    @GetMapping("/api/v1/me/national-identity-requests")
    List<NationalIdentityRequest> mine(@AuthenticationPrincipal Jwt jwt) { return queryService.mine(accountId(jwt)); }

    @GetMapping("/api/v1/me/national-identity-requests/{requestId}/history")
    List<NationalIdentityRequestStatusChange> history(@PathVariable UUID requestId, @AuthenticationPrincipal Jwt jwt) { return queryService.history(accountId(jwt), requestId); }

    @PostMapping(value = "/api/v1/me/national-identity-requests/{requestId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<AttachmentResponse> uploadAttachment(@PathVariable UUID requestId, @RequestParam AttachmentDocumentType documentType, @RequestPart("file") MultipartFile file, @AuthenticationPrincipal Jwt jwt) {
        NationalIdentityRequestAttachment attachment = attachmentService.upload(accountId(jwt), requestId, documentType, new AttachmentUpload(file.getOriginalFilename(), file.getContentType(), file.getSize(), inputStream(file)));
        return ResponseEntity.status(HttpStatus.CREATED).body(AttachmentResponse.from(attachment));
    }

    @GetMapping("/api/v1/me/national-identity-requests/{requestId}/attachments")
    List<AttachmentResponse> myAttachments(@PathVariable UUID requestId, @AuthenticationPrincipal Jwt jwt) { return attachmentService.listMine(accountId(jwt), requestId).stream().map(AttachmentResponse::from).toList(); }

    @GetMapping("/api/v1/me/national-identity-requests/{requestId}/attachment-requirements")
    AttachmentRequirements myAttachmentRequirements(@PathVariable UUID requestId, @AuthenticationPrincipal Jwt jwt) { return attachmentService.requirementsMine(accountId(jwt), requestId); }

    @GetMapping("/api/v1/me/national-identity-requests/{requestId}/attachments/{attachmentId}/content")
    ResponseEntity<InputStreamResource> myAttachmentContent(@PathVariable UUID requestId, @PathVariable UUID attachmentId, @AuthenticationPrincipal Jwt jwt) { return attachmentResponse(attachmentService.downloadMine(accountId(jwt), requestId, attachmentId)); }

    @GetMapping("/api/v1/operations/national-identity-requests")
    @PreAuthorize("hasAuthority('PERM_REQUEST_VIEW')")
    List<NationalIdentityRequest> list(@RequestParam(defaultValue = "SUBMITTED") NationalIdentityRequestStatus status, @AuthenticationPrincipal Jwt jwt) { return queryService.byStatus(accountId(jwt), status); }

    @GetMapping("/api/v1/operations/national-identity-requests/{requestId}/attachments")
    @PreAuthorize("hasAuthority('PERM_REQUEST_VIEW')")
    List<AttachmentResponse> operationalAttachments(@PathVariable UUID requestId, @AuthenticationPrincipal Jwt jwt) { return attachmentService.listForOperations(accountId(jwt), requestId).stream().map(AttachmentResponse::from).toList(); }

    @GetMapping("/api/v1/operations/national-identity-requests/{requestId}/attachment-requirements")
    @PreAuthorize("hasAuthority('PERM_REQUEST_VIEW')")
    AttachmentRequirements operationalAttachmentRequirements(@PathVariable UUID requestId, @AuthenticationPrincipal Jwt jwt) { return attachmentService.requirementsForOperations(accountId(jwt), requestId); }

    @GetMapping("/api/v1/operations/national-identity-requests/{requestId}/attachments/{attachmentId}/content")
    @PreAuthorize("hasAuthority('PERM_REQUEST_VIEW')")
    ResponseEntity<InputStreamResource> operationalAttachmentContent(@PathVariable UUID requestId, @PathVariable UUID attachmentId, @AuthenticationPrincipal Jwt jwt) { return attachmentResponse(attachmentService.downloadForOperations(accountId(jwt), requestId, attachmentId)); }

    @PostMapping("/api/v1/operations/national-identity-requests/{requestId}/review")
    @PreAuthorize("hasAuthority('PERM_REQUEST_REVIEW')")
    NationalIdentityRequest review(@PathVariable UUID requestId, @AuthenticationPrincipal Jwt jwt) { return reviewService.startReview(accountId(jwt), requestId); }

    @PostMapping("/api/v1/operations/national-identity-requests/{requestId}/decision")
    @PreAuthorize("(#body.approved and hasAuthority('PERM_REQUEST_APPROVE')) or (!#body.approved and hasAuthority('PERM_REQUEST_REJECT'))")
    NationalIdentityRequest decide(@PathVariable UUID requestId, @Valid @RequestBody Decision body, @AuthenticationPrincipal Jwt jwt) { return reviewService.decide(accountId(jwt), requestId, body.approved(), body.reason()); }

    private static AccountId accountId(Jwt jwt) { return new AccountId(UUID.fromString(jwt.getSubject())); }
    private static java.io.InputStream inputStream(MultipartFile file) { try { return file.getInputStream(); } catch (java.io.IOException exception) { throw new IllegalArgumentException("Unable to read attachment", exception); } }
    private static ResponseEntity<InputStreamResource> attachmentResponse(NationalIdentityAttachmentContent content) { NationalIdentityRequestAttachment attachment = content.attachment(); return ResponseEntity.ok().contentType(MediaType.parseMediaType(attachment.contentType())).contentLength(attachment.sizeBytes()).header("X-Content-Type-Options", "nosniff").header("Cache-Control", "private, no-store").header("Content-Disposition", ContentDisposition.attachment().filename(attachment.originalFileName()).build().toString()).body(new InputStreamResource(content.content())); }
    public record AttachmentResponse(UUID id, AttachmentDocumentType documentType, String fileName, String contentType, long sizeBytes, java.time.Instant uploadedAt) { static AttachmentResponse from(NationalIdentityRequestAttachment attachment) { return new AttachmentResponse(attachment.id(), attachment.documentType(), attachment.originalFileName(), attachment.contentType(), attachment.sizeBytes(), attachment.uploadedAt()); } }
    public record Submission(
            @NotNull NationalIdentityRequestKind kind,
            @Size(max = 1000) String reason,
            @NotNull RequestBeneficiaryType beneficiaryType,
            UUID dependentBirthCertificateId,
            @NotBlank @Size(max = 128) String paymentReference,
            @Size(max = 128) String lossReportNumber
    ) {
        ServiceRequestSubmissionDetails toDetails() {
            return new ServiceRequestSubmissionDetails(beneficiaryType, dependentBirthCertificateId,
                    paymentReference, lossReportNumber);
        }
    }
    public record Decision(boolean approved, @Size(max = 1000) String reason) { }
}

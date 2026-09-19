package com.chari.chariapp.exception;

import com.chari.chariapp.birthrequest.application.BirthCertificateRequestConflictException;
import com.chari.chariapp.document.application.DocumentAlreadyExistsException;
import com.chari.chariapp.document.application.DocumentNotFoundException;
import com.chari.chariapp.identityrequest.application.NationalIdentityRequestConflictException;
import com.chari.chariapp.request.application.PassportRequestConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class UnifiedServiceRequestErrorTests {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void usesTheSameOpenRequestErrorForPassportIdentityAndBirthCertificate() {
        assertError(handler.handlePassportRequestConflict(new PassportRequestConflictException(
                PassportRequestConflictException.Reason.OPEN_REQUEST_EXISTS
        )), HttpStatus.CONFLICT, "OPEN_REQUEST_EXISTS");
        assertError(handler.handleIdentityRequestConflict(new NationalIdentityRequestConflictException(
                NationalIdentityRequestConflictException.Reason.OPEN_REQUEST_EXISTS
        )), HttpStatus.CONFLICT, "OPEN_REQUEST_EXISTS");
        assertError(handler.handleBirthRequestConflict(new BirthCertificateRequestConflictException(
                BirthCertificateRequestConflictException.Reason.OPEN_REQUEST_EXISTS
        )), HttpStatus.CONFLICT, "OPEN_REQUEST_EXISTS");
    }

    @Test
    void distinguishesExistingAndMissingDocumentsWithStableCodes() {
        assertError(handler.handleDocumentAlreadyExists(new DocumentAlreadyExistsException()),
                HttpStatus.CONFLICT, "DOCUMENT_ALREADY_EXISTS");
        assertError(handler.handleDocumentNotFound(new DocumentNotFoundException()),
                HttpStatus.NOT_FOUND, "DOCUMENT_NOT_FOUND");
    }

    private void assertError(ResponseEntity<EnrollmentErrorResponse> response, HttpStatus status, String code) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(code);
        assertThat(response.getBody().status()).isEqualTo(status.value());
    }
}

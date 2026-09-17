package com.chari.chariapp.exception;

import com.chari.chariapp.account.application.AccountAlreadyExistsException;
import com.chari.chariapp.account.application.EnrollmentProofUnavailableException;
import com.chari.chariapp.account.application.EnrollmentUnavailableException;
import com.chari.chariapp.account.application.InvalidEnrollmentOtpException;
import com.chari.chariapp.account.application.InvalidCredentialsException;
import com.chari.chariapp.account.application.InvalidRefreshTokenException;
import com.chari.chariapp.account.application.WeakPasswordException;
import com.chari.chariapp.citizen.application.CitizenPhoneVerificationException;
import com.chari.chariapp.request.application.PassportRequestConflictException;
import com.chari.chariapp.request.application.AttachmentUploadException;
import com.chari.chariapp.request.domain.PassportRequestTransitionException;
import com.chari.chariapp.identityrequest.application.NationalIdentityRequestConflictException;
import com.chari.chariapp.identityrequest.domain.NationalIdentityRequestTransitionException;
import com.chari.chariapp.birthrequest.application.BirthCertificateRequestConflictException;
import com.chari.chariapp.birthrequest.domain.BirthCertificateRequestTransitionException;
import com.chari.chariapp.appointment.application.AppointmentConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), 404));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequest(BadRequestException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage(), 400));
    }

    @ExceptionHandler(EnrollmentUnavailableException.class)
    public ResponseEntity<EnrollmentErrorResponse> handleEnrollmentUnavailable(EnrollmentUnavailableException ex) {
        log.warn("Enrollment unavailable: {} ({})", ex.getClass().getSimpleName(), ex.getMessage());
        return switch (ex.reason()) {
            case NATIONAL_ID_NOT_FOUND -> enrollmentError(
                    HttpStatus.NOT_FOUND,
                    "NATIONAL_ID_NOT_FOUND",
                    "رقم الهوية غير موجود"
            );
            case CITIZEN_ACCOUNT_EXISTS -> enrollmentError(
                    HttpStatus.CONFLICT,
                    "CITIZEN_ACCOUNT_EXISTS",
                    "يوجد حساب مسجل لهذه الهوية"
            );
            case EMAIL_ALREADY_EXISTS -> enrollmentError(
                    HttpStatus.CONFLICT,
                    "EMAIL_ALREADY_EXISTS",
                    "البريد الإلكتروني مستخدم مسبقًا"
            );
        };
    }

    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ResponseEntity<EnrollmentErrorResponse> handleAccountAlreadyExists(AccountAlreadyExistsException ex) {
        log.warn("Account creation conflict: {}", ex.getMessage());
        return enrollmentError(
                HttpStatus.CONFLICT,
                "ACCOUNT_ALREADY_EXISTS",
                "يوجد حساب مسجل بهذه البيانات"
        );
    }

    @ExceptionHandler(EnrollmentProofUnavailableException.class)
    public ResponseEntity<EnrollmentErrorResponse> handleEnrollmentProofUnavailable(
            EnrollmentProofUnavailableException ex
    ) {
        log.warn("Enrollment proof rejected: {}", ex.getMessage());
        return enrollmentError(
                "ENROLLMENT_PROOF_INVALID",
                "انتهت صلاحية التحقق أو تم استخدامه؛ اطلب رمزًا جديدًا"
        );
    }

    @ExceptionHandler(InvalidEnrollmentOtpException.class)
    public ResponseEntity<EnrollmentErrorResponse> handleInvalidEnrollmentOtp(InvalidEnrollmentOtpException ex) {
        log.warn("Enrollment OTP rejected: {}", ex.getMessage());
        return enrollmentError(
                "INVALID_OTP",
                "رمز التحقق غير صحيح أو انتهت صلاحيته"
        );
    }

    @ExceptionHandler(WeakPasswordException.class)
    public ResponseEntity<EnrollmentErrorResponse> handleWeakPassword(WeakPasswordException ex) {
        return enrollmentError(
                "WEAK_PASSWORD",
                "كلمة المرور يجب أن تتكون من 12 إلى 128 حرفًا"
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<EnrollmentErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(error.getField(), validationMessage(error.getField()))
        );
        log.warn("Client validation rejected fields: {}", fieldErrors.keySet());
        return ResponseEntity.badRequest().body(new EnrollmentErrorResponse(
                "VALIDATION_ERROR",
                "بيانات الطلب غير صحيحة",
                HttpStatus.BAD_REQUEST.value(),
                fieldErrors
        ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<EnrollmentErrorResponse> handleUnreadableRequest(HttpMessageNotReadableException ex) {
        log.warn("Unreadable request body: {}", ex.getMessage());
        return enrollmentError(
                "MALFORMED_JSON",
                "صيغة JSON غير صحيحة أو أحد الحقول من نوع غير متوقع"
        );
    }

    @ExceptionHandler({
            CitizenPhoneVerificationException.class,
            AttachmentUploadException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<EnrollmentErrorResponse> handleInvalidInput(Exception ex) {
        log.warn("Client input rejected: {} ({})", ex.getClass().getSimpleName(), ex.getMessage());
        return enrollmentError("INVALID_REQUEST", "بيانات الطلب غير صحيحة");
    }

    @ExceptionHandler({InvalidCredentialsException.class, InvalidRefreshTokenException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationFailure(Exception ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Authentication failed", HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler({
            PassportRequestConflictException.class,
            PassportRequestTransitionException.class,
            NationalIdentityRequestConflictException.class,
            NationalIdentityRequestTransitionException.class,
            BirthCertificateRequestConflictException.class,
            BirthCertificateRequestTransitionException.class,
            AppointmentConflictException.class
    })
    public ResponseEntity<ErrorResponse> handlePassportRequestConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage(), HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Something went wrong", 500));
    }

    private ResponseEntity<EnrollmentErrorResponse> enrollmentError(String code, String message) {
        return enrollmentError(HttpStatus.BAD_REQUEST, code, message);
    }

    private ResponseEntity<EnrollmentErrorResponse> enrollmentError(
            HttpStatus status,
            String code,
            String message
    ) {
        return ResponseEntity.status(status).body(new EnrollmentErrorResponse(
                code, message, status.value()
        ));
    }

    private String validationMessage(String field) {
        return switch (field) {
            case "nationalId" -> "رقم الهوية مطلوب ويجب أن يتكون من 6 إلى 32 حرفًا أو رقمًا";
            case "phoneNumber" -> "رقم الهاتف مطلوب بالصيغة الدولية مثل +23599123456";
            case "email" -> "البريد الإلكتروني غير صالح";
            case "password" -> "كلمة المرور يجب أن تتكون من 12 إلى 128 حرفًا";
            case "challengeId" -> "معرّف عملية التحقق مطلوب";
            case "code" -> "رمز التحقق يجب أن يتكون من 6 أرقام";
            default -> "القيمة غير صحيحة";
        };
    }
}

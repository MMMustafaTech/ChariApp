package com.chari.chariapp.exception;

import com.chari.chariapp.account.application.AccountAlreadyExistsException;
import com.chari.chariapp.additionaldocument.domain.AdditionalDocumentRequestConflictException;
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
import com.chari.chariapp.document.application.DocumentAlreadyExistsException;
import com.chari.chariapp.document.application.DocumentNotFoundException;
import com.chari.chariapp.document.application.DocumentIssuanceException;
import org.springframework.security.access.AccessDeniedException;
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
    public ResponseEntity<EnrollmentErrorResponse> handleNotFound(NotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return enrollmentError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "الطلب أو المورد غير موجود");
    }

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<EnrollmentErrorResponse> handleDocumentNotFound(DocumentNotFoundException ex) {
        return enrollmentError(HttpStatus.NOT_FOUND, "DOCUMENT_NOT_FOUND", "الوثيقة غير موجودة");
    }

    @ExceptionHandler(DocumentAlreadyExistsException.class)
    public ResponseEntity<EnrollmentErrorResponse> handleDocumentAlreadyExists(DocumentAlreadyExistsException ex) {
        return enrollmentError(HttpStatus.CONFLICT, "DOCUMENT_ALREADY_EXISTS", "الوثيقة موجودة مسبقًا");
    }

    @ExceptionHandler(DocumentIssuanceException.class)
    public ResponseEntity<EnrollmentErrorResponse> handleDocumentIssuance(DocumentIssuanceException ex) {
        return enrollmentError(HttpStatus.CONFLICT, ex.code(),
                "لا تتوفر بيانات رسمية كافية لإصدار الوثيقة");
    }

    @ExceptionHandler(FeatureDisabledException.class)
    public ResponseEntity<EnrollmentErrorResponse> handleFeatureDisabled(FeatureDisabledException ex) {
        String message = switch (ex.code()) {
            case "REQUEST_SUBMISSIONS_DISABLED" -> "استقبال الطلبات متوقف مؤقتًا";
            case "NOTIFICATIONS_DISABLED" -> "إرسال الإشعارات متوقف مؤقتًا";
            default -> "الخدمة متوقفة مؤقتًا";
        };
        return enrollmentError(HttpStatus.SERVICE_UNAVAILABLE, ex.code(), message);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<EnrollmentErrorResponse> handleBadRequest(BadRequestException ex) {
        return enrollmentError(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "بيانات الطلب غير صحيحة");
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
        Map<String, String> newbornFieldErrors = newbornFieldErrors(rootCauseMessage(ex));
        if (!newbornFieldErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(new EnrollmentErrorResponse(
                    "BIRTH_REQUEST_VALIDATION_ERROR",
                    "بيانات تسجيل المولود غير صحيحة",
                    HttpStatus.BAD_REQUEST.value(),
                    newbornFieldErrors
            ));
        }
        return enrollmentError(
                "MALFORMED_JSON",
                "صيغة JSON غير صحيحة أو أحد الحقول من نوع غير متوقع"
        );
    }

    @ExceptionHandler(AttachmentUploadException.class)
    public ResponseEntity<EnrollmentErrorResponse> handleAttachmentUpload(AttachmentUploadException ex) {
        log.warn("Attachment rejected: {}", ex.getMessage());
        return enrollmentError("ATTACHMENT_INVALID", attachmentMessage(ex.getMessage()));
    }

    @ExceptionHandler({CitizenPhoneVerificationException.class, IllegalArgumentException.class})
    public ResponseEntity<EnrollmentErrorResponse> handleInvalidInput(Exception ex) {
        log.warn("Client input rejected: {} ({})", ex.getClass().getSimpleName(), ex.getMessage());
        return enrollmentError("INVALID_REQUEST", invalidRequestMessage(ex.getMessage()));
    }

    @ExceptionHandler({InvalidCredentialsException.class, InvalidRefreshTokenException.class})
    public ResponseEntity<EnrollmentErrorResponse> handleAuthenticationFailure(Exception ex) {
        return enrollmentError(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "بيانات تسجيل الدخول غير صحيحة أو انتهت الجلسة");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<EnrollmentErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return enrollmentError(HttpStatus.FORBIDDEN, "FORBIDDEN", "لا تملك صلاحية تنفيذ هذه العملية");
    }

    @ExceptionHandler(AppointmentConflictException.class)
    public ResponseEntity<EnrollmentErrorResponse> handleAppointmentConflict(AppointmentConflictException ex) {
        return enrollmentError(HttpStatus.CONFLICT, "APPOINTMENT_CONFLICT", "يوجد تعارض مع الموعد المطلوب");
    }

    @ExceptionHandler(AdditionalDocumentRequestConflictException.class)
    public ResponseEntity<EnrollmentErrorResponse> handleAdditionalDocumentConflict(AdditionalDocumentRequestConflictException ex) {
        String message = switch (ex.code()) {
            case "ADDITIONAL_DOCUMENTS_ALREADY_REQUESTED" -> "يوجد طلب مستندات إضافية مفتوح مسبقًا";
            case "ADDITIONAL_DOCUMENTS_INCOMPLETE" -> "يجب رفع ملف واحد على الأقل لكل مستند مطلوب";
            case "ADDITIONAL_DOCUMENTS_UPLOAD_CLOSED" -> "تم إرسال المستندات ولا يمكن إضافة ملفات جديدة";
            case "ADDITIONAL_DOCUMENTS_NOT_SUBMITTED" -> "لم يرسل المواطن المستندات المطلوبة بعد";
            case "ADDITIONAL_DOCUMENTS_UNRESOLVED" -> "يجب مراجعة المستندات الإضافية وإغلاقها قبل اتخاذ القرار";
            case "ADDITIONAL_DOCUMENTS_REVIEWER_MISMATCH" -> "الموظف الذي يراجع الطلب هو فقط من يستطيع إدارة المستندات المطلوبة";
            default -> "لا تسمح حالة طلب المستندات الحالية بهذه العملية";
        };
        return enrollmentError(HttpStatus.CONFLICT, ex.code(), message);
    }

    @ExceptionHandler(PassportRequestConflictException.class)
    public ResponseEntity<EnrollmentErrorResponse> handlePassportRequestConflict(PassportRequestConflictException ex) {
        return requestConflict(ex.reason().name());
    }

    @ExceptionHandler(NationalIdentityRequestConflictException.class)
    public ResponseEntity<EnrollmentErrorResponse> handleIdentityRequestConflict(NationalIdentityRequestConflictException ex) {
        return requestConflict(ex.reason().name());
    }

    @ExceptionHandler(BirthCertificateRequestConflictException.class)
    public ResponseEntity<EnrollmentErrorResponse> handleBirthRequestConflict(
            BirthCertificateRequestConflictException ex
    ) {
        return requestConflict(ex.reason().name());
    }

    @ExceptionHandler(PassportRequestTransitionException.class)
    public ResponseEntity<EnrollmentErrorResponse> handlePassportRequestTransition(PassportRequestTransitionException ex) {
        return requestTransition(ex.reason().name());
    }

    @ExceptionHandler(NationalIdentityRequestTransitionException.class)
    public ResponseEntity<EnrollmentErrorResponse> handleIdentityRequestTransition(NationalIdentityRequestTransitionException ex) {
        return requestTransition(ex.reason().name());
    }

    @ExceptionHandler(BirthCertificateRequestTransitionException.class)
    public ResponseEntity<EnrollmentErrorResponse> handleBirthRequestTransition(
            BirthCertificateRequestTransitionException ex
    ) {
        return requestTransition(ex.reason().name());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<EnrollmentErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled request failure", ex);
        return enrollmentError(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "حدث خطأ داخلي؛ حاول مرة أخرى لاحقًا");
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

    private ResponseEntity<EnrollmentErrorResponse> requestConflict(String reason) {
        return switch (reason) {
            case "OPEN_REQUEST_EXISTS" -> enrollmentError(
                    HttpStatus.CONFLICT, "OPEN_REQUEST_EXISTS", "يوجد طلب مفتوح مسبقًا"
            );
            case "ATTACHMENTS_CLOSED" -> enrollmentError(
                    HttpStatus.CONFLICT, "REQUEST_ATTACHMENTS_CLOSED", "لا يمكن إضافة مرفقات بعد بدء مراجعة الطلب"
            );
            case "SELF_REVIEW_NOT_ALLOWED" -> enrollmentError(
                    HttpStatus.CONFLICT, "SELF_REVIEW_NOT_ALLOWED", "لا يمكن للموظف مراجعة طلبه الشخصي"
            );
            case "MISSING_REQUIRED_ATTACHMENTS" -> enrollmentError(
                    HttpStatus.CONFLICT, "MISSING_REQUIRED_ATTACHMENTS", "لا يمكن بدء المراجعة قبل رفع كل المستندات المطلوبة"
            );
            default -> enrollmentError(HttpStatus.CONFLICT, "REQUEST_CONFLICT", "تتعارض العملية مع حالة الطلب الحالية");
        };
    }

    private ResponseEntity<EnrollmentErrorResponse> requestTransition(String reason) {
        return switch (reason) {
            case "NOT_AWAITING_REVIEW" -> enrollmentError(
                    HttpStatus.CONFLICT, "REQUEST_NOT_AWAITING_REVIEW", "الطلب ليس بانتظار بدء المراجعة"
            );
            case "NOT_UNDER_REVIEW" -> enrollmentError(
                    HttpStatus.CONFLICT, "REQUEST_NOT_UNDER_REVIEW", "يجب أن يكون الطلب قيد المراجعة قبل اتخاذ القرار"
            );
            case "REVIEWER_MISMATCH" -> enrollmentError(
                    HttpStatus.CONFLICT, "REQUEST_REVIEWER_MISMATCH", "الموظف الذي بدأ المراجعة هو فقط من يستطيع اتخاذ القرار"
            );
            case "REJECTION_REASON_REQUIRED" -> enrollmentError(
                    HttpStatus.BAD_REQUEST, "REJECTION_REASON_REQUIRED", "سبب الرفض مطلوب"
            );
            default -> enrollmentError(HttpStatus.CONFLICT, "INVALID_REQUEST_TRANSITION", "لا تسمح حالة الطلب الحالية بهذه العملية");
        };
    }

    private String validationMessage(String field) {
        return switch (field) {
            case "nationalId" -> "رقم الهوية مطلوب ويجب أن يتكون من 6 إلى 32 حرفًا أو رقمًا";
            case "phoneNumber" -> "رقم الهاتف مطلوب بالصيغة الدولية مثل +23599123456";
            case "email" -> "البريد الإلكتروني غير صالح";
            case "password" -> "كلمة المرور يجب أن تتكون من 12 إلى 128 حرفًا";
            case "challengeId" -> "معرّف عملية التحقق مطلوب";
            case "code" -> "رمز التحقق يجب أن يتكون من 6 أرقام";
            case "kind" -> "نوع الطلب مطلوب";
            case "reason" -> "السبب يجب ألا يتجاوز 1000 حرف";
            default -> "القيمة غير صحيحة";
        };
    }

    private String attachmentMessage(String message) {
        if (message == null) return "المرفق غير صالح";
        if (message.contains("size")) return "حجم المرفق يجب أن يكون بين 1 بايت و5 ميجابايت";
        if (message.contains("JPEG")) return "يسمح فقط بملفات JPEG وPNG وPDF";
        if (message.contains("declared type")) return "محتوى الملف لا يطابق نوعه المعلن";
        return "المرفق غير صالح";
    }

    private String invalidRequestMessage(String message) {
        if (message == null) return "بيانات الطلب غير صحيحة";
        if (message.contains("Newborn registration details are required")) return "بيانات المولود مطلوبة لطلب تسجيل مولود";
        if (message.contains("Newborn details are only valid")) return "بيانات المولود مسموحة فقط لطلب تسجيل مولود";
        if (message.contains("requires a reason")) return "سبب الطلب مطلوب لهذا النوع";
        if (message.contains("reason must not exceed")) return "سبب الطلب يجب ألا يتجاوز 1000 حرف";
        return "بيانات الطلب غير صحيحة";
    }

    private String rootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage();
    }

    private Map<String, String> newbornFieldErrors(String message) {
        if (message == null) return Map.of();
        Map<String, String> errors = new LinkedHashMap<>();
        if (message.startsWith("Child first name")) errors.put("newbornRegistration.childFirstName", "اسم الطفل الأول مطلوب وبحد أقصى 100 حرف");
        else if (message.startsWith("Child last name")) errors.put("newbornRegistration.childLastName", "اسم عائلة الطفل مطلوب وبحد أقصى 100 حرف");
        else if (message.startsWith("Date of birth")) errors.put("newbornRegistration.dateOfBirth", "تاريخ الميلاد مطلوب ولا يمكن أن يكون في المستقبل");
        else if (message.startsWith("Place of birth")) errors.put("newbornRegistration.placeOfBirth", "مكان الميلاد مطلوب وبحد أقصى 200 حرف");
        else if (message.startsWith("Gender")) errors.put("newbornRegistration.gender", "جنس المولود مطلوب");
        else if (message.startsWith("Father full name")) errors.put("newbornRegistration.fatherFullName", "اسم الأب الكامل مطلوب وبحد أقصى 200 حرف");
        else if (message.startsWith("Father national ID")) errors.put("newbornRegistration.fatherNationalId", "رقم هوية الأب يجب أن يتكون من 5 إلى 32 رقمًا");
        else if (message.startsWith("Mother full name")) errors.put("newbornRegistration.motherFullName", "اسم الأم الكامل مطلوب وبحد أقصى 200 حرف");
        else if (message.startsWith("Mother national ID")) errors.put("newbornRegistration.motherNationalId", "رقم هوية الأم يجب أن يتكون من 5 إلى 32 رقمًا");
        return errors;
    }
}

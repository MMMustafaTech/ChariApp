package com.chari.chariapp.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

public record EnrollmentErrorResponse(
        String code,
        String message,
        int status,
        LocalDateTime timestamp,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) Map<String, String> fieldErrors
) {
    public EnrollmentErrorResponse(String code, String message, int status) {
        this(code, message, status, LocalDateTime.now(), Map.of());
    }

    public EnrollmentErrorResponse(
            String code,
            String message,
            int status,
            Map<String, String> fieldErrors
    ) {
        this(code, message, status, LocalDateTime.now(), Map.copyOf(fieldErrors));
    }
}

package com.chari.chariapp.account.application;

import com.chari.chariapp.citizen.domain.PhoneReference;

public record RequestEnrollmentOtpCommand(
        String nationalIdLookup,
        PhoneReference phone,
        String emailLookup
) {
}

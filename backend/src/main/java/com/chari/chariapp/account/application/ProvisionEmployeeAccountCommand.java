package com.chari.chariapp.account.application;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.account.domain.StaffPermission;
import com.chari.chariapp.citizen.domain.PhoneReference;

import java.util.Set;

public record ProvisionEmployeeAccountCommand(
        AccountId administratorId,
        String emailLookup,
        String encryptedEmail,
        String rawPassword,
        String firstName,
        String lastName,
        PhoneReference phone,
        String jobTitle,
        Set<StaffPermission> permissions
) {
    public ProvisionEmployeeAccountCommand(AccountId administratorId, String emailLookup, String encryptedEmail, String rawPassword) {
        this(administratorId, emailLookup, encryptedEmail, rawPassword, null, null, null, null,
                StaffPermission.employeeDefaults());
    }
}

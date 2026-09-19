package com.chari.chariapp.account.application;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.account.domain.StaffPermission;
import com.chari.chariapp.citizen.domain.PhoneReference;

import java.util.Set;

public record UpdateStaffAccountCommand(
        AccountId administratorId,
        AccountId employeeId,
        String firstName,
        String lastName,
        PhoneReference phone,
        String jobTitle,
        Set<StaffPermission> permissions
) {
}

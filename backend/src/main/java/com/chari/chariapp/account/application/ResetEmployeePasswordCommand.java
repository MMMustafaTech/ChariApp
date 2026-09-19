package com.chari.chariapp.account.application;

import com.chari.chariapp.account.domain.AccountId;

public record ResetEmployeePasswordCommand(AccountId administratorId, AccountId employeeId, String temporaryPassword) {
}

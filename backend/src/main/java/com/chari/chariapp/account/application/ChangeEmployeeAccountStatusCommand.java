package com.chari.chariapp.account.application;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.account.domain.AccountStatus;


public record ChangeEmployeeAccountStatusCommand(AccountId administratorId, AccountId employeeId, AccountStatus status) {
}

package com.chari.chariapp.account.application;

import com.chari.chariapp.account.domain.AccountId;

import java.util.List;

public interface EmployeeAccountAdministrationUseCase {
    AccountId provisionEmployee(ProvisionEmployeeAccountCommand command);
    void changeEmployeeStatus(ChangeEmployeeAccountStatusCommand command);
    List<StaffAccountView> listStaff(AccountId administratorId);
    StaffAccountView getStaff(AccountId administratorId, AccountId staffAccountId);
    void updateEmployee(UpdateStaffAccountCommand command);
    void resetEmployeePassword(ResetEmployeePasswordCommand command);
}

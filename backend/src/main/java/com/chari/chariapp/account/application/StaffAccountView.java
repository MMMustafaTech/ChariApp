package com.chari.chariapp.account.application;

import com.chari.chariapp.account.domain.Account;
import com.chari.chariapp.account.domain.StaffProfile;

public record StaffAccountView(Account account, StaffProfile profile) {
}

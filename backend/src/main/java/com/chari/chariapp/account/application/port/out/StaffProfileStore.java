package com.chari.chariapp.account.application.port.out;

import com.chari.chariapp.account.domain.AccountId;
import com.chari.chariapp.account.domain.StaffProfile;

import java.util.List;
import java.util.Optional;

public interface StaffProfileStore {
    Optional<StaffProfile> findByAccountId(AccountId accountId);
    List<StaffProfile> findAll();
    StaffProfile save(StaffProfile profile);
}

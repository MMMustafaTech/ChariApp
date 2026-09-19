package com.chari.chariapp.account.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Collection;
import java.util.List;

import com.chari.chariapp.account.domain.AccountRole;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataAccountRepository extends JpaRepository<AccountJpaEntity, String> {

    boolean existsByEmailLookup(String emailLookup);

    boolean existsByCitizenId(String citizenId);

    Optional<AccountJpaEntity> findByCitizenId(String citizenId);

    Optional<AccountJpaEntity> findByEmailLookup(String emailLookup);

    @org.springframework.data.jpa.repository.Query("select a from AccountJpaEntity a where a.citizenId in (select c.id from CitizenJpaEntity c where c.nationalIdLookup = :lookup)")
    Optional<AccountJpaEntity> findByNationalIdLookup(@org.springframework.data.repository.query.Param("lookup") String lookup);

    @Query("select distinct a from AccountJpaEntity a join a.roles r where r.code in :roles")
    List<AccountJpaEntity> findByRoleCodes(@Param("roles") Collection<AccountRole> roles);
}

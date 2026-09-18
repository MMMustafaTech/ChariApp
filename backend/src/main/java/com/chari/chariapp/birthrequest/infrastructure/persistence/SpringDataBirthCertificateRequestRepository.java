package com.chari.chariapp.birthrequest.infrastructure.persistence;
import com.chari.chariapp.birthrequest.domain.BirthCertificateRequestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
interface SpringDataBirthCertificateRequestRepository extends JpaRepository<BirthCertificateRequestJpaEntity,String> {
    @Query("select count(r) > 0 from BirthCertificateRequestJpaEntity r where r.citizenId = :citizenId and r.type = 'BIRTH_CERTIFICATE' and r.openRequestType = :openRequestType") boolean hasOpen(@Param("citizenId") String citizenId, @Param("openRequestType") String openRequestType);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select r from BirthCertificateRequestJpaEntity r where r.id = :id") Optional<BirthCertificateRequestJpaEntity> findForUpdate(@Param("id") String id);
    List<BirthCertificateRequestJpaEntity> findByTypeAndStatusOrderBySubmittedAtAsc(String type, BirthCertificateRequestStatus status);
    List<BirthCertificateRequestJpaEntity> findByTypeAndCitizenIdOrderBySubmittedAtDesc(String type,String citizenId);
}

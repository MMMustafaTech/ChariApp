package com.chari.chariapp.birthrequest.application.port.out;
import com.chari.chariapp.birthrequest.domain.*;
import com.chari.chariapp.citizen.domain.CitizenId;
import java.util.*;
public interface BirthCertificateRequestStore { boolean hasOpenRequest(CitizenId citizenId, BirthCertificateRequestKind kind); Optional<BirthCertificateRequest> findById(UUID id); Optional<BirthCertificateRequest> findByIdForUpdate(UUID id); BirthCertificateRequest save(BirthCertificateRequest request); List<BirthCertificateRequest> findByStatus(BirthCertificateRequestStatus status); List<BirthCertificateRequest> findByCitizenId(CitizenId citizenId); }

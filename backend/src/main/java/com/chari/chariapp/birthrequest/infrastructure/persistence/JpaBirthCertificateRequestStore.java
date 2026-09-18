package com.chari.chariapp.birthrequest.infrastructure.persistence;
import com.chari.chariapp.birthrequest.application.port.out.BirthCertificateRequestStore;
import com.chari.chariapp.birthrequest.domain.*;
import com.chari.chariapp.citizen.domain.CitizenId;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository public class JpaBirthCertificateRequestStore implements BirthCertificateRequestStore {
    private final SpringDataBirthCertificateRequestRepository repository;
    public JpaBirthCertificateRequestStore(SpringDataBirthCertificateRequestRepository repository){this.repository=repository;}
    public boolean hasOpenRequest(CitizenId citizenId, BirthCertificateRequestKind kind){return repository.hasOpen(citizenId.value().toString(), BirthCertificateRequestJpaEntity.openRequestType(kind));}
    public Optional<BirthCertificateRequest> findById(UUID id){return repository.findById(id.toString()).filter(BirthCertificateRequestJpaEntity::isBirthCertificateRequest).map(BirthCertificateRequestJpaEntity::toDomain);}
    public Optional<BirthCertificateRequest> findByIdForUpdate(UUID id){return repository.findForUpdate(id.toString()).filter(BirthCertificateRequestJpaEntity::isBirthCertificateRequest).map(BirthCertificateRequestJpaEntity::toDomain);}
    public BirthCertificateRequest save(BirthCertificateRequest request){return repository.findById(request.id().toString()).map(e->{e.apply(request);return repository.save(e);}).orElseGet(()->repository.save(BirthCertificateRequestJpaEntity.from(request))).toDomain();}
    public List<BirthCertificateRequest> findByStatus(BirthCertificateRequestStatus status){return repository.findByTypeAndStatusOrderBySubmittedAtAsc(BirthCertificateRequestJpaEntity.TYPE,status).stream().map(BirthCertificateRequestJpaEntity::toDomain).toList();}
    public List<BirthCertificateRequest> findByCitizenId(CitizenId citizenId){return repository.findByTypeAndCitizenIdOrderBySubmittedAtDesc(BirthCertificateRequestJpaEntity.TYPE,citizenId.value().toString()).stream().map(BirthCertificateRequestJpaEntity::toDomain).toList();}
}

package com.chari.chariapp.additionaldocument.infrastructure.persistence;

import com.chari.chariapp.additionaldocument.application.port.out.AdditionalDocumentRequestStore;
import com.chari.chariapp.additionaldocument.domain.AdditionalDocumentRequest;
import com.chari.chariapp.additionaldocument.domain.AdditionalDocumentRequestStatus;
import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.operations.domain.ServiceRequestType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class JpaAdditionalDocumentRequestStore implements AdditionalDocumentRequestStore {
    private final SpringDataAdditionalDocumentRequestRepository repository;
    JpaAdditionalDocumentRequestStore(SpringDataAdditionalDocumentRequestRepository repository){this.repository=repository;}
    public AdditionalDocumentRequest save(AdditionalDocumentRequest request){
        var entity=repository.findById(request.id().toString()).orElseGet(()->AdditionalDocumentRequestJpaEntity.from(request));
        entity.apply(request); return repository.save(entity).toDomain();
    }
    public Optional<AdditionalDocumentRequest> findById(UUID id){return repository.findById(id.toString()).map(AdditionalDocumentRequestJpaEntity::toDomain);}
    public List<AdditionalDocumentRequest> findByCitizenId(CitizenId id){return repository.findByCitizenIdOrderByRequestedAtDesc(id.value().toString()).stream().map(AdditionalDocumentRequestJpaEntity::toDomain).toList();}
    public List<AdditionalDocumentRequest> findByOriginalRequest(ServiceRequestType type,UUID id){return repository.findByServiceTypeAndRequestIdOrderByRequestedAtDesc(type,id.toString()).stream().map(AdditionalDocumentRequestJpaEntity::toDomain).toList();}
    public boolean hasOpenRequest(ServiceRequestType type,UUID id){return repository.existsByServiceTypeAndRequestIdAndStatusIn(type,id.toString(),List.of(AdditionalDocumentRequestStatus.REQUESTED,AdditionalDocumentRequestStatus.SUBMITTED));}
}

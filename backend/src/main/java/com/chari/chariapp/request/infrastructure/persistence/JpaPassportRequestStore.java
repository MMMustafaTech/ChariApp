package com.chari.chariapp.request.infrastructure.persistence;

import com.chari.chariapp.citizen.domain.CitizenId;
import com.chari.chariapp.request.application.port.out.PassportRequestStore;
import com.chari.chariapp.request.domain.PassportRequest;
import com.chari.chariapp.request.domain.PassportRequestStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaPassportRequestStore implements PassportRequestStore {

    private static final String PASSPORT_TYPE = "PASSPORT";

    private final SpringDataPassportRequestRepository repository;
    private final EncryptedServiceRequestPayloadCodec payloadCodec;

    public JpaPassportRequestStore(SpringDataPassportRequestRepository repository,
                                   EncryptedServiceRequestPayloadCodec payloadCodec) {
        this.repository = repository;
        this.payloadCodec = payloadCodec;
    }

    @Override
    public boolean hasOpenRequest(CitizenId citizenId) {
        return repository.hasOpen(citizenId.value().toString());
    }

    @Override
    public Optional<PassportRequest> findById(UUID requestId) {
        return repository.findById(requestId.toString()).map(value -> value.toDomain(payloadCodec));
    }

    @Override
    public Optional<PassportRequest> findByIdForUpdate(UUID requestId) {
        return repository.findForUpdate(requestId.toString()).map(value -> value.toDomain(payloadCodec));
    }

    @Override
    public PassportRequest save(PassportRequest request) {
        return repository.findById(request.id().toString())
                .map(entity -> {
                    entity.apply(request, payloadCodec);
                    return repository.save(entity);
                })
                .orElseGet(() -> repository.save(PassportRequestJpaEntity.from(request, payloadCodec)))
                .toDomain(payloadCodec);
    }

    @Override
    public List<PassportRequest> findByStatus(PassportRequestStatus status) {
        return repository.findByTypeAndStatusOrderBySubmittedAtAsc(PASSPORT_TYPE, status).stream()
                .map(value -> value.toDomain(payloadCodec))
                .toList();
    }

    @Override
    public List<PassportRequest> findByCitizenId(CitizenId citizenId) {
        return repository.findByTypeAndCitizenIdOrderBySubmittedAtDesc(PASSPORT_TYPE, citizenId.value().toString()).stream()
                .map(value -> value.toDomain(payloadCodec))
                .toList();
    }
}

package com.chari.chariapp.service;

import com.chari.chariapp.exception.NotFoundException;
import com.chari.chariapp.dto.NationalIdResponse;
import com.chari.chariapp.repository.NormalizedCitizenDocumentRepository;
import org.springframework.stereotype.Service;

@Service

public class NationalIdService {

    private final NormalizedCitizenDocumentRepository
            repository;

    public NationalIdService(NormalizedCitizenDocumentRepository repository) {
        this.repository = repository;

    }

    public NationalIdResponse getByIdNumber(String nationalIdNumber) {
        return repository.nationalIdentity(nationalIdNumber)
                .orElseThrow(() -> new NotFoundException("National ID not found"));
    }

}

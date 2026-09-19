package com.chari.chariapp.service;

import com.chari.chariapp.dto.BirthCertificateResponse;
import com.chari.chariapp.exception.NotFoundException;
import com.chari.chariapp.repository.NormalizedCitizenDocumentRepository;
import org.springframework.stereotype.Service;

@Service
public class BirthCertificateService {

    private final NormalizedCitizenDocumentRepository repository;

    public BirthCertificateService(NormalizedCitizenDocumentRepository repository) {
        this.repository = repository;
    }

    public BirthCertificateResponse getByNationalId(String nationalId) {
        return repository.birthCertificate(nationalId)
                .orElseThrow(() -> new NotFoundException("Birth certificate not found"));
    }
}

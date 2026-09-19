package com.chari.chariapp.service;

import com.chari.chariapp.dto.PassportResponse;
import com.chari.chariapp.exception.NotFoundException;
import com.chari.chariapp.repository.NormalizedCitizenDocumentRepository;
import org.springframework.stereotype.Service;

@Service
public class PassportService {

    private final NormalizedCitizenDocumentRepository passportRepository;

    public PassportService(NormalizedCitizenDocumentRepository PassportRepository) {
        this.passportRepository = PassportRepository;
    }

    public PassportResponse getPassportByNationalId(String nationalId) {
        return passportRepository.passport(nationalId)
                .orElseThrow(() -> new NotFoundException("Passport not found"));
    }
}

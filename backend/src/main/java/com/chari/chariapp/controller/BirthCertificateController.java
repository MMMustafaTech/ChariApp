package com.chari.chariapp.controller;

import com.chari.chariapp.service.BirthCertificateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/birth-certificate")
public class BirthCertificateController {

    private final BirthCertificateService service;

    public BirthCertificateController(BirthCertificateService service) {
        this.service = service;
    }

    @GetMapping("/{nationalId}")
    public ResponseEntity<?> get(@PathVariable String nationalId) {

        return ResponseEntity.ok(service.getByNationalId(nationalId));
    }
}

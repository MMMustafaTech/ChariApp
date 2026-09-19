package com.chari.chariapp.controller;


import com.chari.chariapp.service.PassportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/passport")
public class PassportController {
    private final PassportService passportService;

    public PassportController(PassportService passportService) {
        this.passportService = passportService;
    }

    @GetMapping("/{nationalId}")
    public ResponseEntity<?> getPassport(@PathVariable String nationalId) {

        return ResponseEntity.ok(passportService.getPassportByNationalId(nationalId));
    }
}

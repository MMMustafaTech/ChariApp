package com.chari.chariapp.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "birth_certificate_legacy")
@Getter
@Setter
public class BirthCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nationalId;
    private String certificateNumber;

    private String fullName;
    private String gender;
    private LocalDate birthDate;
    private String birthPlace;

    private String fatherName;
    private LocalDate fatherBirthDate;
    private String fatherBirthPlace;
    private String fatherProfession;

    private String motherName;
    private LocalDate motherBirthDate;
    private String motherBirthPlace;
    private String motherProfession;

    private LocalDate declarationDate;
    private String address;

    private LocalDateTime createdAt;
}

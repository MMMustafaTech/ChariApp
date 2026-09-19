package com.chari.chariapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "passports_legacy")
public class Passport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "mother_name")
    private String motherName;

    private String nationality;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "place_of_birth")
    private String placeOfBirth;

    @Column(name = "date_of_issue")
    private LocalDate dateOfIssue;

    @Column(name = "date_of_expiry")
    private LocalDate dateOfExpiry;

    @Column(name = "national_id_number", unique = true)
    private String nationalIdNumber;

    private String sex;

    private String job;

    @Column(name = "place_of_issue")
    private String placeOfIssue;

    @Column(name = "issuing_authority")
    private String issuingAuthority;

    @Column(name = "passport_number", unique = true)
    private String passportNumber;
}

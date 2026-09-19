package com.chari.chariapp.service;

import com.chari.chariapp.dto.UserResponse;
import com.chari.chariapp.dto.PassportResponse;
import com.chari.chariapp.entity.User;
import com.chari.chariapp.exception.BadRequestException;
import com.chari.chariapp.exception.NotFoundException;
import com.chari.chariapp.repository.NormalizedCitizenDocumentRepository;
import com.chari.chariapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final NormalizedCitizenDocumentRepository documents;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       NormalizedCitizenDocumentRepository documents,
                       PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.documents = documents;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(String nationalId, String password, String email) {

        if (!documents.nationalIdentityExists(nationalId)) {
            throw new NotFoundException("National ID does not exist");
        }

        if (userRepository.existsByNationalIdNumber(nationalId)) {
            throw new BadRequestException("User already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already used");
        }

        User user = new User();
        user.setNationalIdNumber(nationalId);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);

        userRepository.save(user);

        PassportResponse passport = documents
                .passport(nationalId)
                .orElseThrow(() -> new NotFoundException("Passport not found"));

        return new UserResponse(
                user.getId(),
                user.getNationalIdNumber(),
                user.getEmail(),
                passport.getFirstName()
        );
    }


    public UserResponse login(String nationalId, String password) {
        User user = userRepository.findByNationalIdNumber(nationalId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }
        PassportResponse passport = documents
                .passport(nationalId)
                .orElseThrow(() -> new NotFoundException("Passport not found"));

        return new UserResponse(
                user.getId(),
                user.getNationalIdNumber(),
                user.getEmail(),
                passport.getFirstName()
        );
    }
}

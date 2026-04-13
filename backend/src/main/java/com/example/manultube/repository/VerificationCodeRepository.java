package com.example.manultube.repository;

import com.example.manultube.model.VerificationCode;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface VerificationCodeRepository
        extends MongoRepository<VerificationCode, String> {

    Optional<VerificationCode> findByEmail(String email);

    void deleteByEmail(String email);
}
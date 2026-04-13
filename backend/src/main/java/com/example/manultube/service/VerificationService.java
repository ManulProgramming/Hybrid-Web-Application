package com.example.manultube.service;

import com.example.manultube.model.VerificationCode;
import com.example.manultube.repository.VerificationCodeRepository;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class VerificationService {
    private final VerificationCodeRepository repo;
    private final PasswordEncoder encoder = new Argon2PasswordEncoder(32, 64, 1, 15 * 1024, 2);
    private static final int MAX_ATTEMPTS = 5;
    private final EmailService emailService;
    public VerificationService(VerificationCodeRepository repo, EmailService emailService) {
        this.repo = repo;
        this.emailService = emailService;
    }

    public void sendVerificationCode(String email) {
        SecureRandom random = new SecureRandom();
        String code = String.valueOf(random.nextInt(900000) + 100000);

        String hash = encoder.encode(code);

        VerificationCode entity = new VerificationCode();
        entity.setEmail(email);
        entity.setCodeHash(hash);
        entity.setExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES));

        repo.deleteByEmail(email);
        repo.save(entity);

        emailService.sendEmail(email, code);
    }

    public boolean verifyCode(String email, String inputCode) {

        Optional<VerificationCode> optional = repo.findByEmail(email);

        if (optional.isEmpty()) return false;

        VerificationCode stored = optional.get();

        if (stored.getAttempts() >= MAX_ATTEMPTS) {
            repo.deleteByEmail(email);
            return false;
        }

        boolean matches = encoder.matches(inputCode, stored.getCodeHash());

        if (matches) {
            repo.deleteByEmail(email);
            return true;
        } else {
            stored.setAttempts(stored.getAttempts() + 1);
            repo.save(stored);
            return false;
        }
    }
}
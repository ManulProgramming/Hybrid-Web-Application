package com.example.manultube.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "verification_codes")
public class VerificationCode {
    @Id
    private String id;

    private String email;
    private String codeHash;

    @Indexed(name = "ttl_index", expireAfter = "0")
    private Instant expiresAt;

    private int attempts;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getCodeHash() {
        return codeHash;
    }
    public void setCodeHash(String codeHash) {
        this.codeHash = codeHash;
    }
    public Instant getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
    public int getAttempts() {
        return attempts;
    }
    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }
}

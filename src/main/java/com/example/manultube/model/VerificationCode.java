package com.example.manultube.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Setter
@Getter
@Document(collection = "verification_codes")
public class VerificationCode {
    @Id
    private String id;

    private String email;
    private String codeHash;

    @Indexed(name = "ttl_index", expireAfter = "0")
    private Instant expiresAt;

    private int attempts;

}

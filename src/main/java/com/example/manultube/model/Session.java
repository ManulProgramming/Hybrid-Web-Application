package com.example.manultube.model;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Setter
@Getter
@Document(collection = "sessions")
public class Session {
    @Id
    private String id;
    private String token;
    private Long userId;
    private Long expiresIn;

}

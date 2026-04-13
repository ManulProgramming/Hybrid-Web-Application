package com.example.manultube.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Setter
@Getter
public class APIError {
    private HttpStatus status;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> errors;
    public APIError(HttpStatus status, String message, Map<String, String> errors) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.errors = errors;
    }

}

package com.example.manultube.model;

import java.time.LocalDateTime;
import java.util.Map;

public class APIError {
    private int status;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> errors;
    public APIError(int status, String message, Map<String, String> errors) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.errors = errors;
    }
    public int getStatus() {
        return status;
    }
    public String getMessage() {
        return message;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public Map<String, String> getErrors() {
        return errors;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
}

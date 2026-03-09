package com.example.manultube.dto.Session;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class SessionRequestDTO {
    private String token;
    @NotBlank
    @Min(1)
    private Long userId;
    private Long expiresIn;
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public Long getExpiresIn() {
        return expiresIn;
    }
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}

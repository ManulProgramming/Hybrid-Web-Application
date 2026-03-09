package com.example.manultube.dto.Post;

import jakarta.validation.constraints.Pattern;

public class PostUpdateDTO {
    private Long userId;
    @Pattern(regexp = "^.{0,100}$", message = "Invalid title. Title should not be empty and be less than 100 characters long")
    private String title;
    @Pattern(regexp = ".{0,2000}", message = "Invalid description. Description cannot be empty or more than 2000 characters long")
    private String description;
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}

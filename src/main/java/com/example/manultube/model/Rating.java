package com.example.manultube.model;

import jakarta.validation.constraints.NotBlank;

public class Rating {
    private Long userId;
    private Long postId;
    @NotBlank
    private Boolean rating;

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public Long getPostId() {
        return postId;
    }
    public void setPostId(Long postId) {
        this.postId = postId;
    }
    public Boolean getRating() {
        return rating;
    }
    public void setRating(Boolean rating) {
        this.rating = rating;
    }
}

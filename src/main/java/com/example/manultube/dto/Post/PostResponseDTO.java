package com.example.manultube.dto.Post;

public class PostResponseDTO {
    private Long id;
    private Long userId;
    private String username;
    private String title;
    private String description;
    private Integer upvotes;
    private Integer downvotes;
    private Long createdAt;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
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
    public Integer getUpvotes() {
        return upvotes;
    }
    public void setUpvotes(Integer upvotes) {
        this.upvotes = upvotes;
    }
    public Integer getDownvotes() {
        return downvotes;
    }
    public void setDownvotes(Integer downvotes) {
        this.downvotes = downvotes;
    }
    public Long getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}

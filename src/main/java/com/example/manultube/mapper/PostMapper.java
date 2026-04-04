package com.example.manultube.mapper;

import com.example.manultube.dto.Post.PostRequestDTO;
import com.example.manultube.dto.Post.PostResponseDTO;
import com.example.manultube.model.Post;

public class PostMapper {
    public static PostResponseDTO toDto(Post entity) {
        PostResponseDTO dto = new PostResponseDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setUsername(entity.getUsername());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setUpvotes(entity.getUpvotes());
        dto.setDownvotes(entity.getDownvotes());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
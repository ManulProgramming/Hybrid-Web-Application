package com.example.manultube.mapper;

import com.example.manultube.dto.Comment.CommentResponseDTO;
import com.example.manultube.dto.Rating.RatingResponseDTO;
import com.example.manultube.model.Comment;
import com.example.manultube.model.Rating;

public class CommentMapper {
    public static CommentResponseDTO toDto(Comment entity) {
        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setId(entity.getId());
        dto.setComment(entity.getComment());
        dto.setUserId(entity.getUser().getId());
        dto.setUsername(entity.getUser().getUsername());
        dto.setPostId(entity.getPost().getId());
        return dto;
    }
}

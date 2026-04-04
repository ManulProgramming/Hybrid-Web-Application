package com.example.manultube.mapper;

import com.example.manultube.dto.Rating.RatingResponseDTO;
import com.example.manultube.model.Rating;

public class RatingMapper {
    public static RatingResponseDTO toDto(Rating entity) {
        RatingResponseDTO dto = new RatingResponseDTO();
        dto.setId(entity.getId());
        dto.setRating(entity.getRating());
        dto.setUserId(entity.getUser().getId());
        dto.setPostId(entity.getPost().getId());
        return dto;
    }
}

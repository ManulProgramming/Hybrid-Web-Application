package com.example.manultube.dto.Rating;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RatingResponseDTO {
    private Long id;
    private Long userId;
    private Long postId;
    private Boolean rating;
}
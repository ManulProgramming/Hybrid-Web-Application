package com.example.manultube.dto.Rating;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Schema(description = "Rating entity")
public class RatingRequestDTO {
    @Schema(hidden = true)
    private Long userId;
    @Schema(hidden = true)
    private Long postId;
    @Schema(description = "Rating data. true = like; false = dislike")
    private Boolean rating;
}
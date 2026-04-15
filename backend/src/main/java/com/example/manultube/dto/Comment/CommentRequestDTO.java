package com.example.manultube.dto.Comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Comment entity")
public class CommentRequestDTO {
    @Schema(hidden = true)
    private Long userId;
    @Schema(hidden = true)
    private String username;
    @Schema(hidden = true)
    private Long postId;
    @Schema(description = "Comment itself")
    private String comment;
}
package com.example.manultube.dto.Comment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentResponseDTO {
    private Long id;
    private Long userId;
    private String username;
    private Long postId;
    private String comment;
}
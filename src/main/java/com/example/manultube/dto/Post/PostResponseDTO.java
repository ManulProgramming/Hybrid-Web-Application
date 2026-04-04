package com.example.manultube.dto.Post;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PostResponseDTO {
    private Long id;
    private Long userId;
    private String username;
    private String title;
    private String description;
    private Integer upvotes;
    private Integer downvotes;
    private Long createdAt;

}

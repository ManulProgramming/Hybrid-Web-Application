package com.example.manultube.dto.Post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "Post update entity")
public class PostUpdateDTO {
    @Schema(hidden = true)
    private Long userId;
    @Schema(description = "New post title")
    @Pattern(regexp = "^.{0,100}$", message = "Invalid title. Title should not be empty and be less than 100 characters long")
    private String title;
    @Schema(description = "New post description")
    @Size(min=0, max=2000, message = "Invalid description. Description cannot be empty or more than 2000 characters long")
    private String description;
}
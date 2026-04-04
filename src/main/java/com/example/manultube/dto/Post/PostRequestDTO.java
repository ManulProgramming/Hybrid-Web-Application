package com.example.manultube.dto.Post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PostRequestDTO {
    private Long userId;
    @Pattern(regexp = "^[a-zA-Z0-9._-]{1,50}$", message = "Invalid username. Username can only contain lowercase and uppercase characters, dot, underscore and dash and be no more than 50 characters long")
    private String username;
    @Pattern(regexp = "^.{1,100}$", message = "Invalid title. Title should not be empty and be less than 100 characters long")
    @NotBlank
    private String title;
    @Size(min=0, max=2000, message = "Invalid description. Description cannot be empty or more than 2000 characters long")
    private String description;
    private Integer upvotes;
    private Integer downvotes;
    private Long createdAt;

}

package com.example.manultube.dto.Post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
@Schema(description = "Post entity")
public class PostRequestDTO {
    @Schema(hidden = true)
    private Long userId;
    @Schema(hidden = true)
    @Pattern(regexp = "^[a-zA-Z0-9._-]{1,50}$", message = "Invalid username. Username can only contain lowercase and uppercase characters, dot, underscore and dash and be no more than 50 characters long")
    private String username;
    @Schema(description = "Post title", example = "Lorem Ipsum")
    @Pattern(regexp = "^.{1,100}$", message = "Invalid title. Title should not be empty and be less than 100 characters long")
    @NotBlank
    private String title;
    @Schema(description = "Post description", example = "Lorem Ipsum")
    @Size(min=0, max=2000, message = "Invalid description. Description cannot be empty or more than 2000 characters long")
    private String description;
    @Schema(hidden = true)
    private Integer upvotes;
    @Schema(hidden = true)
    private Integer downvotes;
    @Schema(hidden = true)
    private Long createdAt;
    @Schema(description = "File form data")
    @NotNull
    private MultipartFile file;
}
package com.example.manultube.dto.User;

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
@Schema(description = "User register entity")
public class UserRegisterDTO {
    @Schema(description = "User name", example = "John")
    @Pattern(regexp = "^[a-zA-Z0-9._-]{1,50}$", message = "Invalid username. Username can only contain lowercase and uppercase characters, dot, underscore and dash and be no more than 50 characters long")
    @NotBlank
    private String username;
    @Schema(description = "User password (needs to meet security requirements)")
    @Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$", message = "Invalid password. Password requires at least 8 characters, 1 uppercase, 1 lowercase, 1 number, and 1 special character")
    @NotBlank
    private String userpass;
    @Schema(description = "User email", example = "john.doe@example.com")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",message = "Invalid usermail.")
    @Size(max=89,message="Email cannot be more than 89 characters long.")
    @NotBlank
    private String usermail;
    @Schema(description = "Email code received from /api/code method")
    @Pattern(regexp = "^[0-9]{6}$",message="Invalid code.")
    @NotBlank
    private String code;
    @Schema(description = "Optional avatar image")
    private MultipartFile file;
}
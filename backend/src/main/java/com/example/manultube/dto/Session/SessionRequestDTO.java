package com.example.manultube.dto.Session;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SessionRequestDTO {
    private String token;
    @NotBlank
    @Min(1)
    private Long userId;
    private Long expiresIn;

}

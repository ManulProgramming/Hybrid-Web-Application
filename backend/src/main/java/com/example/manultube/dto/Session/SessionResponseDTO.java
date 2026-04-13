package com.example.manultube.dto.Session;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SessionResponseDTO {
    private String id;
    private String token;
    private Long userId;
    private Long expiresIn;

}

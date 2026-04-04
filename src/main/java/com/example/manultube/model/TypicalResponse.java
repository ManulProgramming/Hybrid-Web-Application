package com.example.manultube.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Setter
@Getter
public class TypicalResponse<T> {
    private HttpStatus status;
    private Map<String, Object> currentUser;
    private T content;
    private Map<String, Object> params;

}

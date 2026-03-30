package com.example.manultube.dto.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UserLoginDTO {
    @Pattern(regexp = "^([a-zA-Z0-9._-]{1,50}|[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$", message = "Invalid username. Username can only contain lowercase and uppercase characters, dot, underscore and dash and be no more than 50 characters long")
    @NotBlank
    private String username;
    @Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$", message = "Invalid password. Password requires at least 8 characters, 1 uppercase, 1 lowercase, 1 number, and 1 special character")
    @NotBlank
    private String userpass;
    @Pattern(regexp = "^[0-9]{6}$",message="Invalid code.")
    @NotBlank
    private String code;

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getUserpass() {
        return userpass;
    }
    public void setUserpass(String userpass) {
        this.userpass = userpass;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

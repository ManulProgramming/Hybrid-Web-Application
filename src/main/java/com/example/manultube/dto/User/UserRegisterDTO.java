package com.example.manultube.dto.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserRegisterDTO {
    @Pattern(regexp = "^[a-zA-Z0-9._-]{1,50}$", message = "Invalid username. Username can only contain lowercase and uppercase characters, dot, underscore and dash and be no more than 50 characters long")
    @NotBlank
    private String username;
    @Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$", message = "Invalid password. Password requires at least 8 characters, 1 uppercase, 1 lowercase, 1 number, and 1 special character")
    @NotBlank
    private String userpass;
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",message = "Invalid usermail.")
    @Size(max=89,message="Email cannot be more than 89 characters long.")
    @NotBlank
    private String usermail;
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsermail() {
        return usermail;
    }

    public void setUsermail(String usermail) {
        this.usermail = usermail;
    }

    public String getUserpass() {
        return userpass;
    }
    public void setUserpass(String userpass) {
        this.userpass = userpass;
    }
}

package com.warroom.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String name;

    @NotBlank
    private String dob;

    @NotBlank
    private String role;

    @NotBlank
    private String password;
}

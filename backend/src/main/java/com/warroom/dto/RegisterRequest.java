package com.warroom.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String name;
    private String dob;
    private String role;
    private String password;
}

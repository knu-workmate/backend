package com.workmate.workmate.auth.dto;

import lombok.Getter;

@Getter
public class LoginRequest {
    final String email;
    final String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

}

package com.workmate.workmate.user.dto;

import lombok.Getter;

@Getter
public class PasswordRequest {
    private String currentPassword;
    private String newPassword;

    public PasswordRequest(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }
}

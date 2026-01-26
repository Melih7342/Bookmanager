package com.melih.bookmanager.utils;

import lombok.Data;

@Data
public class UserChangePasswordRequest {
    private String oldPassword;
    private String newPassword;
}

package com.melih.bookmanager.utils;

import lombok.Data;

@Data
public class UserAuthenticationRequest {
    private String username;
    private String password;
}

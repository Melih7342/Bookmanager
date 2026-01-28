package com.melih.bookmanager.utils;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserAuthenticationRequest {
    @NotBlank(message = "Username darf nicht leer sein")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters long")
    private String username;

    @NotBlank(message = "Passwort darf nicht leer sein")
    @Size(min = 8, message = "Password must have at least 8 characters")
    @Pattern(regexp = ".*[0-9].*", message = "Passwort must contain at least one number")
    private String password;
}

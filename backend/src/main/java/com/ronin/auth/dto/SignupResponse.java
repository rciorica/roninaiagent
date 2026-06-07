package com.ronin.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponse {
    private String email;
    private String username;
    private String token;
    private String message;

    public SignupResponse(String email, String username, String message) {
        this.email = email;
        this.username = username;
        this.message = message;
    }
}

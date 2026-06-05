package com.ronin.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Step 2 of two-step login: User enters password with session ID
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordVerificationRequest {
    private String sessionId;
    private String password;
}

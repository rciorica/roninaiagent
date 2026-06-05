package com.ronin.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Step 1 of two-step login: User enters email/username
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationRequest {
    private String email;
}

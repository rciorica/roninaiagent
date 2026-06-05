package com.ronin.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Step 1 response: User data preloaded in background
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationResponse {
    private boolean userExists;
    private String email;
    private String username;
    
    @JsonProperty("rank")
    private UserRankDto rank;
    
    @JsonProperty("sessionId")
    private String sessionId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRankDto {
        private String name;
        private String beltColor;
        private int kyuLevel;
    }
}

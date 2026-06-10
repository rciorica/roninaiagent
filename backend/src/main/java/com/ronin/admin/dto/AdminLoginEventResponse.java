package com.ronin.admin.dto;

import java.time.LocalDateTime;

public record AdminLoginEventResponse(
    long id,
    String userEmail,
    LocalDateTime loginTime,
    boolean success
) {
}

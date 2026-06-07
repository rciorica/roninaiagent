package com.ronin.admin.dto;

public record AdminUserResponse(
    long userId,
    String email,
    String displayName,
    int completedProjects
) {
}

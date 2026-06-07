package com.ronin.admin.dto;

public record AdminProjectResponse(
    long projectId,
    String name,
    String ownerEmail,
    String status,
    String phase
) {
}

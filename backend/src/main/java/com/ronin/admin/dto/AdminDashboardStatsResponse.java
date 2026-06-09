package com.ronin.admin.dto;

public record AdminDashboardStatsResponse(
    long totalUsers,
    long totalProjects,
    long totalLogins,
    long failedLogins,
    long uniqueUsersLoggedInToday
) {
}

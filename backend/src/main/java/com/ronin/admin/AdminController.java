package com.ronin.admin;

import com.ronin.admin.dto.AdminDashboardStatsResponse;
import com.ronin.admin.dto.AdminLoginEventResponse;
import com.ronin.admin.dto.AdminProjectResponse;
import com.ronin.admin.dto.AdminUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard/stats")
    public AdminDashboardStatsResponse getDashboardStats() {
        return adminService.getDashboardStats();
    }

    @GetMapping("/users/stats")
    public List<AdminUserResponse> getUsersStats() {
        return adminService.getUsersStats();
    }

    @GetMapping("/projects/stats")
    public List<AdminProjectResponse> getProjectsStats() {
        return adminService.getProjectsStats();
    }

    @GetMapping("/login-events")
    public List<AdminLoginEventResponse> getLoginEvents() {
        return adminService.getLoginEvents();
    }

    @GetMapping("/summary")
    public AdminDashboardStatsResponse getSummary() {
        return adminService.getDashboardStats();
    }
}

package com.ronin.admin;

import com.ronin.admin.dto.AdminDashboardStatsResponse;
import com.ronin.admin.dto.AdminLoginEventResponse;
import com.ronin.admin.dto.AdminProjectResponse;
import com.ronin.admin.dto.AdminUserResponse;
import com.ronin.projects.ProjectEntity;
import com.ronin.projects.ProjectRepository;
import com.ronin.users.UserEntity;
import com.ronin.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public AdminDashboardStatsResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalProjects = projectRepository.count();
        
        return new AdminDashboardStatsResponse(
            totalUsers,
            totalProjects,
            0L,
            0L,
            0L
        );
    }

    public List<AdminUserResponse> getUsersStats() {
        return userRepository.findAll()
                .stream()
                .limit(10)
                .map(user -> new AdminUserResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getDisplayName(),
                    user.getCompletedProjects()
                ))
                .collect(Collectors.toList());
    }

    public List<AdminProjectResponse> getProjectsStats() {
        return projectRepository.findAll()
                .stream()
                .limit(10)
                .map(project -> new AdminProjectResponse(
                    project.getId(),
                    project.getName(),
                    project.getUser().getEmail(),
                    project.getStatus().toString(),
                    project.getPhase().toString()
                ))
                .collect(Collectors.toList());
    }

    public List<AdminLoginEventResponse> getLoginEvents() {
        // Return empty list for now - login events would need to be tracked separately
        return List.of();
    }
}

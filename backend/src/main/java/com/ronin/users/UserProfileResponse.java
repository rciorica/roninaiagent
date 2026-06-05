package com.ronin.users;

import com.ronin.ranking.RankEntity;

public class UserProfileResponse {

    private Long id;
    private String email;
    private int completedProjects;
    private RankEntity rank;
    private int projectsToNextRank;

    public UserProfileResponse() {}

    public UserProfileResponse(Long id, String email, int completedProjects, RankEntity rank, int projectsToNextRank) {
        this.id = id;
        this.email = email;
        this.completedProjects = completedProjects;
        this.rank = rank;
        this.projectsToNextRank = projectsToNextRank;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getCompletedProjects() {
        return completedProjects;
    }

    public void setCompletedProjects(int completedProjects) {
        this.completedProjects = completedProjects;
    }

    public RankEntity getRank() {
        return rank;
    }

    public void setRank(RankEntity rank) {
        this.rank = rank;
    }

    public int getProjectsToNextRank() {
        return projectsToNextRank;
    }

    public void setProjectsToNextRank(int projectsToNextRank) {
        this.projectsToNextRank = projectsToNextRank;
    }
}

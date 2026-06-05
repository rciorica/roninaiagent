package com.ronin.projects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ronin.projects.enums.ProjectPhase;
import com.ronin.projects.enums.ProjectStatus;
import com.ronin.users.UserEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "projects")
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    private ProjectPhase phase;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    @Column(name = "repo_url")
    private String repoUrl;

    @ManyToOne
    private UserEntity user;

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ProjectPhase getPhase() { return phase; }
    public void setPhase(ProjectPhase phase) { this.phase = phase; }
    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }
    public String getRepoUrl() { return repoUrl; }
    public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }

    @JsonIgnore
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
}

package com.ronin.projects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ronin.users.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project_messages")
public class ProjectMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private String sender;

    @Column(name = "model_used")
    private String modelUsed;

    private String message;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ProjectMessageAttachmentEntity> attachments = new ArrayList<>();

    @Column(name = "validation_issues", columnDefinition = "TEXT")
    private String validationIssues;

    public Long getId() { return id; }

    @JsonIgnore
    public ProjectEntity getProject() { return project; }
    public void setProject(ProjectEntity project) { this.project = project; }

    @JsonIgnore
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public List<ProjectMessageAttachmentEntity> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<ProjectMessageAttachmentEntity> attachments) {
        this.attachments = attachments;
    }

    public String getValidationIssues() { return validationIssues; }
    public void setValidationIssues(String validationIssues) { this.validationIssues = validationIssues; }
}

package com.ronin.projects;

import java.util.List;

public class ProjectArtifactResponse {

    private Long projectId;
    private String artifactUrl;
    private String description;
    private List<ProjectArtifactFileEntity> files;

    public ProjectArtifactResponse(Long projectId, String artifactUrl, String description, List<ProjectArtifactFileEntity> files) {
        this.projectId = projectId;
        this.artifactUrl = artifactUrl;
        this.description = description;
        this.files = files;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getArtifactUrl() {
        return artifactUrl;
    }

    public String getDescription() {
        return description;
    }

    public List<ProjectArtifactFileEntity> getFiles() {
        return files;
    }
}

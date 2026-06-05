package com.ronin.projects;

import com.ronin.auth.CurrentUserService;
import com.ronin.projects.ProjectArtifactFileRepository;
import com.ronin.projects.ProjectArtifactResponse;
import com.ronin.projects.ProjectMessageAttachmentEntity;
import com.ronin.projects.dto.CreateProjectRequest;
import com.ronin.projects.enums.ProjectStatus;
import com.ronin.users.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMessageRepository messageRepository;
    private final ProjectArtifactFileRepository artifactFileRepository;
    private final ProjectGeneratorService projectGeneratorService;
    private final CurrentUserService currentUserService;

    public List<ProjectEntity> getMyProjects() {
        UserEntity user = currentUserService.get();
        return projectRepository.findAll()
                .stream()
                .filter(p -> p.getUser().getId().equals(user.getId()))
                .toList();
    }

    public ProjectEntity createProject(CreateProjectRequest req) {
        UserEntity user = currentUserService.get();

        ProjectEntity p = new ProjectEntity();
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setPhase(req.getPhase());
        p.setStatus(ProjectStatus.IN_PROGRESS);
        p.setUser(user);

        ProjectEntity saved = projectRepository.save(p);
        projectGeneratorService.startProjectGeneration(saved.getId());
        return saved;
    }

    public ProjectMessageEntity addMessage(Long projectId, String message) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        ProjectMessageEntity m = new ProjectMessageEntity();
        m.setProject(project);
        m.setUser(currentUserService.get());
        m.setSender("USER");
        m.setMessage(message);

        return messageRepository.save(m);
    }

    public ProjectMessageEntity uploadProjectAttachments(Long projectId, List<MultipartFile> files) {
        ProjectEntity project = getProject(projectId);

        ProjectMessageEntity message = new ProjectMessageEntity();
        message.setProject(project);
        message.setUser(currentUserService.get());
        message.setSender("USER");
        message.setMessage("Uploaded " + files.size() + " file(s).");

        for (MultipartFile file : files) {
            try {
                ProjectMessageAttachmentEntity attachment = new ProjectMessageAttachmentEntity();
                attachment.setMessage(message);
                attachment.setFileName(file.getOriginalFilename() == null ? "unnamed" : file.getOriginalFilename());
                attachment.setContentType(file.getContentType());
                attachment.setContent(file.getBytes());
                message.getAttachments().add(attachment);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read uploaded file", e);
            }
        }

        return messageRepository.save(message);
    }

    public List<ProjectMessageEntity> getProjectMessages(Long projectId) {
        getProject(projectId);
        return messageRepository.findByProjectIdOrderByCreatedAtAsc(projectId);
    }

    public List<ProjectArtifactFileEntity> getProjectFiles(Long projectId) {
        getProject(projectId);
        return artifactFileRepository.findByProject_IdOrderByFilePathAsc(projectId);
    }

    public ProjectArtifactFileEntity getProjectFile(Long projectId, String filePath) {
        getProject(projectId);
        return artifactFileRepository.findByProject_IdAndFilePath(projectId, normalizeFilePath(filePath))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));
    }

    public ProjectArtifactFileEntity saveProjectFile(Long projectId, String filePath, String content) {
        ProjectEntity project = getProject(projectId);
        String normalizedPath = normalizeFilePath(filePath);
        validateFilePath(normalizedPath);

        ProjectArtifactFileEntity file = artifactFileRepository
                .findByProject_IdAndFilePath(projectId, normalizedPath)
                .orElseGet(() -> {
                    ProjectArtifactFileEntity entity = new ProjectArtifactFileEntity();
                    entity.setProject(project);
                    entity.setFilePath(normalizedPath);
                    return entity;
                });

        file.setContent(content == null ? "" : content);
        return artifactFileRepository.save(file);
    }

    public void deleteProjectFile(Long projectId, String filePath) {
        ProjectArtifactFileEntity file = getProjectFile(projectId, filePath);
        artifactFileRepository.delete(file);
    }

    public ProjectArtifactResponse getProjectArtifact(Long projectId) {
        ProjectEntity project = getProject(projectId);
        boolean isCompleted = project.getStatus() == ProjectStatus.COMPLETED;
        String artifactUrl = isCompleted ? project.getRepoUrl() : null;
        String description = isCompleted
                ? "The final product is complete. Review the generated files below."
                : project.getStatus() == ProjectStatus.FAILED
                    ? "Project generation failed. No final product is available."
                    : "Project generation is in progress. The final product will be available after completion.";
        return new ProjectArtifactResponse(
                projectId,
                artifactUrl,
                description,
                isCompleted ? artifactFileRepository.findByProject_IdOrderByFilePathAsc(projectId) : List.of()
        );
    }

    public List<ProjectEntity> getProjectsForCurrentUser() {
        UserEntity user = currentUserService.get();
        return projectRepository.findAll()
                .stream()
                .filter(p -> p.getUser().getId().equals(user.getId()))
                .toList();
    }

    private static String normalizeFilePath(String filePath) {
        return filePath.replace("\\", "/").trim();
    }

    private static void validateFilePath(String filePath) {
        if (filePath.isBlank() || filePath.startsWith("/") || filePath.startsWith("\\") || filePath.contains("..")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path");
        }
    }

public ProjectEntity getProject(Long id) {
    ProjectEntity project = projectRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    UserEntity user = currentUserService.get();
    if (!project.getUser().getId().equals(user.getId())) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
    }
    return project;
}
}

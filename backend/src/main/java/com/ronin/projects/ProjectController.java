package com.ronin.projects;

import com.ronin.projects.ProjectArtifactFileEntity;
import com.ronin.projects.ProjectArtifactFileRepository;
import com.ronin.projects.ProjectArtifactResponse;
import com.ronin.projects.ProjectMessageAttachmentEntity;
import com.ronin.projects.ProjectMessageAttachmentRepository;
import com.ronin.projects.dto.CreateProjectRequest;
import com.ronin.projects.dto.ProjectFileUpdateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectArtifactFileRepository artifactFileRepository;
    private final ProjectMessageAttachmentRepository attachmentRepository;

    @GetMapping
    public List<ProjectEntity> getProjects() {
        return projectService.getProjectsForCurrentUser();
    }

    @PostMapping
    public ProjectEntity createProject(@RequestBody CreateProjectRequest req) {
        return projectService.createProject(req);
    }

    @GetMapping("/{id}")
    public ProjectEntity getProject(@PathVariable Long id) {
        return projectService.getProject(id);
    }

    @GetMapping("/{id}/messages")
    public List<ProjectMessageEntity> getProjectMessages(@PathVariable("id") Long id) {
        return projectService.getProjectMessages(id);
    }

    @GetMapping("/{id}/artifact")
    public ProjectArtifactResponse getProjectArtifact(@PathVariable("id") Long id) {
        return projectService.getProjectArtifact(id);
    }

    @GetMapping("/{id}/files")
    public List<ProjectArtifactFileEntity> listProjectFiles(@PathVariable("id") Long id) {
        return projectService.getProjectFiles(id);
    }

    @GetMapping("/{id}/files/{path:.+}")
    public ProjectArtifactFileEntity readProjectFile(@PathVariable("id") Long id,
                                                     @PathVariable("path") String path) {
        return projectService.getProjectFile(id, path);
    }

    @PostMapping("/{id}/files/{path:.+}")
    public ProjectFileUpdateResponse saveProjectFile(@PathVariable("id") Long id,
                                                     @PathVariable("path") String path,
                                                     @RequestBody String content,
                                                     @RequestParam(value = "dryRun", defaultValue = "false") boolean dryRun) {
        String normalizedPath = path.replace("\\", "/");
        String oldContent = null;
        try {
            oldContent = projectService.getProjectFile(id, normalizedPath).getContent();
        } catch (ResponseStatusException ignored) {
            oldContent = "";
        }

        if (dryRun) {
            return new ProjectFileUpdateResponse(normalizedPath, true, oldContent, content == null ? "" : content);
        }

        ProjectArtifactFileEntity saved = projectService.saveProjectFile(id, normalizedPath, content);
        return new ProjectFileUpdateResponse(saved.getFilePath(), false, oldContent, saved.getContent());
    }

    @DeleteMapping("/{id}/files/{path:.+}")
    public ResponseEntity<Void> deleteProjectFile(@PathVariable("id") Long id,
                                                  @PathVariable("path") String path) {
        projectService.deleteProjectFile(id, path);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/messages/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProjectMessageEntity uploadProjectAttachments(@PathVariable("id") Long id,
                                                         @RequestParam("files") List<MultipartFile> files) {
        return projectService.uploadProjectAttachments(id, files);
    }

    @GetMapping("/{id}/artifact/view")
    public ResponseEntity<String> viewProjectArtifact(@PathVariable("id") Long id) {
        ProjectEntity project = projectService.getProject(id);
        if (project.getStatus() != com.ronin.projects.enums.ProjectStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project artifact not available");
        }

        List<ProjectArtifactFileEntity> files = artifactFileRepository.findByProject_IdOrderByFilePathAsc(id);
        return files.stream()
                .filter(file -> file.getFilePath().equalsIgnoreCase("index.html"))
                .findFirst()
                .map(file -> ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(file.getContent()))
                .or(() -> files.stream().filter(file -> file.getFilePath().toLowerCase().endsWith(".html")).findFirst()
                        .map(file -> ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(file.getContent())))
                .or(() -> files.stream().filter(file -> file.getFilePath().equalsIgnoreCase("README.md")).findFirst()
                        .map(file -> ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body("<html><body><pre>" + escapeHtml(file.getContent()) + "</pre></body></html>")))
                .or(() -> files.stream().findFirst().map(file -> ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body("<html><body><pre>" + escapeHtml(file.getContent()) + "</pre></body></html>")))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No artifact files found"));
    }

    @GetMapping("/{id}/artifact/file/{path:.+}")
    public ResponseEntity<String> getProjectArtifactFile(@PathVariable("id") Long id,
                                                         @PathVariable("path") String path) {
        projectService.getProject(id);
        ProjectArtifactFileEntity file = artifactFileRepository
                .findByProject_IdAndFilePath(id, path)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artifact file not found"));
        MediaType mediaType = getMediaType(path);
        return ResponseEntity.ok().contentType(mediaType).body(file.getContent());
    }

    @GetMapping("/{projectId}/messages/{messageId}/attachments/{attachmentId}")
    public ResponseEntity<byte[]> downloadMessageAttachment(
            @PathVariable("projectId") Long projectId,
            @PathVariable("messageId") Long messageId,
            @PathVariable("attachmentId") Long attachmentId) {
        projectService.getProject(projectId);
        ProjectMessageAttachmentEntity attachment = attachmentRepository
                .findByIdAndMessage_Id(attachmentId, messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found"));

        String encoded = URLEncoder.encode(attachment.getFileName(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : attachment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .body(attachment.getContent());
    }

    private static MediaType getMediaType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".css")) {
            return MediaType.valueOf("text/css");
        }
        if (lower.endsWith(".js")) {
            return MediaType.valueOf("application/javascript");
        }
        if (lower.endsWith(".html") || lower.endsWith(".htm")) {
            return MediaType.TEXT_HTML;
        }
        if (lower.endsWith(".md")) {
            return MediaType.valueOf("text/markdown");
        }
        return MediaType.TEXT_PLAIN;
    }

    private static String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    @PostMapping("/{id}/messages")
    public ProjectMessageEntity addMessage(@PathVariable Long id, @RequestBody String message) {
        return projectService.addMessage(id, message);
    }
}

package com.ronin.projects;

import com.ronin.auth.CurrentUserService;
import com.ronin.llm.ModelSelectionResult;
import com.ronin.llm.ModelSelector;
import com.ronin.llm.TokenTracker;
import com.ronin.llm.LLMProviderEntity;
import com.ronin.llm.providers.OpenRouterClient;
import com.ronin.llm.providers.OpenRouterResult;
import com.ronin.projects.ProjectArtifactFileEntity;
import com.ronin.projects.ProjectArtifactFileRepository;
import com.ronin.projects.enums.ProjectPhase;
import com.ronin.projects.enums.ProjectStatus;
import com.ronin.users.UserEntity;
import com.ronin.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectGeneratorService {

    private final ProjectRepository projectRepository;
    private final ProjectMessageRepository messageRepository;
    private final ProjectArtifactFileRepository artifactFileRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final ModelSelector modelSelector;
    private final OpenRouterClient openRouterClient;
    private final TokenTracker tokenTracker;

    @Async("taskExecutor")
    public void startProjectGeneration(Long projectId) {
        generateProject(projectId);
    }

    private void generateProject(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        UserEntity user = project.getUser();
        ModelSelectionResult selection = modelSelector.select(user, project.getPhase());
        LLMProviderEntity provider = selection.chosen();

        log.info("Starting async generation for project {} using provider {}", projectId, provider.getName());
        saveProjectMessage(project, "SYSTEM", provider.getName(), "Project generation has started.");

        try {
            String prompt = buildGenerationPrompt(project);
            OpenRouterResult result = openRouterClient.generate(provider.getName(), prompt);
            tokenTracker.recordUsage(user, provider, result.getTokensUsed());

            String assistantResponse = result.getContent();
            saveProjectMessage(project, "ASSISTANT", provider.getName(), assistantResponse);

            List<ProjectArtifactFileEntity> files = buildArtifactFiles(project, assistantResponse);
            files.forEach(file -> file.setProject(project));
            artifactFileRepository.saveAll(files);

            String artifactPath = "/projects/" + projectId + "/artifact/view";
            project.setRepoUrl(artifactPath);
            project.setStatus(ProjectStatus.COMPLETED);
            projectRepository.save(project);

            UserEntity projectOwner = project.getUser();
            projectOwner.setCompletedProjects(projectOwner.getCompletedProjects() + 1);
            userRepository.save(projectOwner);

            saveProjectMessage(project, "SYSTEM", provider.getName(), "Project completed. Generated files are available from your project product view endpoint at " + artifactPath + ". Open the project in your dashboard to review the final file list and download individual files.");
        } catch (Exception e) {
            log.error("Project generation failed for project {}", projectId, e);
            project.setStatus(ProjectStatus.FAILED);
            projectRepository.save(project);
            saveProjectMessage(project, "SYSTEM", null, "Project generation failed: " + e.getMessage());
        }
    }

    private String buildGenerationPrompt(ProjectEntity project) {
        return "You are an AI software architect responsible for building a complete project for a user." +
                "\nProject Name: " + project.getName() +
                "\nPhase: " + project.getPhase() +
                "\nDescription: " + project.getDescription() +
                "\n\nProvide a concise summary of the operations performed to build this project, include the generated file structure or key artifacts, and say where the completed code is stored.";
    }

    private List<ProjectArtifactFileEntity> buildArtifactFiles(ProjectEntity project, String assistantSummary) {
        List<ProjectArtifactFileEntity> files = new ArrayList<>();

        String projectName = project.getName().replaceAll("\\s+", "-").toLowerCase();
        switch (project.getPhase()) {
            case FRONTEND -> {
                files.add(file("index.html", "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n  <title>" + project.getName() + "</title>\n  <link rel=\"stylesheet\" href=\"/projects/" + project.getId() + "/artifact/file/styles.css\">\n</head>\n<body>\n  <main>\n    <h1>" + project.getName() + "</h1>\n    <p>" + project.getDescription() + "</p>\n  </main>\n  <script src=\"/projects/" + project.getId() + "/artifact/file/script.js\"></script>\n</body>\n</html>"));
                files.add(file("styles.css", "body { font-family: Arial, sans-serif; background: #f8fafc; color: #111827; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; }\nmain { text-align: center; padding: 2rem; background: white; border-radius: 1rem; box-shadow: 0 10px 30px rgba(15,23,42,0.08); }\nh1 { font-size: 2.5rem; margin-bottom: 1rem; }\np { font-size: 1rem; color: #475569; }"));
                files.add(file("script.js", "document.addEventListener('DOMContentLoaded', () => { console.log('Project " + project.getName() + " loaded.'); });"));
            }
            case BACKEND -> {
                files.add(file("README.md", "# " + project.getName() + "\n\n" + project.getDescription() + "\n\nThis backend project contains a simple service generated by Ronin."));
                files.add(file("Application.java", "package com.example;\n\npublic class Application {\n    public static void main(String[] args) {\n        System.out.println(\"Hello from " + project.getName() + "\");\n    }\n}"));
            }
            default -> {
                files.add(file("README.md", "# " + project.getName() + "\n\n" + project.getDescription() + "\n\nProject artifacts generated by Ronin.\n\n" + assistantSummary));
            }
        }

        return files;
    }

    private ProjectArtifactFileEntity file(String path, String content) {
        ProjectArtifactFileEntity file = new ProjectArtifactFileEntity();
        file.setFilePath(path);
        file.setContent(content);
        return file;
    }

    private void saveProjectMessage(ProjectEntity project, String sender, String modelUsed, String message) {
        ProjectMessageEntity projectMessage = new ProjectMessageEntity();
        projectMessage.setProject(project);
        if ("USER".equals(sender)) {
            projectMessage.setUser(currentUserService.get());
        } else {
            projectMessage.setUser(project.getUser());
        }
        projectMessage.setSender(sender);
        projectMessage.setModelUsed(modelUsed);
        projectMessage.setMessage(message);
        messageRepository.save(projectMessage);
    }
}

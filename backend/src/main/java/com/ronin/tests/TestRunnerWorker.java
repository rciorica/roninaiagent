package com.ronin.tests;

import com.ronin.projects.ProjectEntity;
import com.ronin.projects.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
@RequiredArgsConstructor
public class TestRunnerWorker {

    private final ProjectTestRunRepository testRepo;
    private final ProjectService projectService;

    @Async("taskExecutor")
    public void execute(Long runId) {

        ProjectTestRunEntity run = testRepo.findById(runId)
                .orElseThrow(() -> new RuntimeException("Test run not found"));

        run.setStatus("RUNNING");
        testRepo.save(run);

        try {
            String logs = runDockerTests(run);
            run.setLogs(logs);

            if (logs.contains("BUILD SUCCESS") || logs.contains("Tests passed")) {
                run.setStatus("PASSED");
            } else {
                run.setStatus("FAILED");
            }

        } catch (Exception e) {
            run.setStatus("FAILED");
            run.setLogs("ERROR: " + e.getMessage());
        }

        testRepo.save(run);
    }

    private String runDockerTests(ProjectTestRunEntity run) throws Exception {

        ProjectEntity project = run.getProject();

        // Example: mount project folder into /app inside container
        ProcessBuilder pb = new ProcessBuilder(
                "docker", "run", "--rm",
                "-v", "/projects/" + project.getId() + ":/app",
                "ronin-project-tester"
        );

        pb.redirectErrorStream(true);

        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        output.append("\nExit code: ").append(exitCode);

        return output.toString();
    }
}

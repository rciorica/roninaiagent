package com.ronin.tests;

import com.ronin.projects.ProjectEntity;
import com.ronin.projects.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestService {

    private final ProjectService projectService;
    private final ProjectTestRunRepository testRepo;
    private final TestRunnerWorker testRunnerWorker;

    public ProjectTestRunEntity runTests(Long projectId) {

        ProjectEntity project = projectService.getProject(projectId);

        ProjectTestRunEntity run = new ProjectTestRunEntity();
        run.setProject(project);
        run.setStatus("PENDING");

        testRepo.save(run);

        testRunnerWorker.execute(run.getId());

        return run;
    }
}

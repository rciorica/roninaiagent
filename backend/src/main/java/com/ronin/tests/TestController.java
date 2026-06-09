package com.ronin.tests;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tests")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;
    private final ProjectTestRunRepository testRepo;

    @PostMapping("/run/{projectId}")
    public ProjectTestRunEntity run(@PathVariable Long projectId) {
        return testService.runTests(projectId);
    }

    @GetMapping("/{runId}")
    public ProjectTestRunEntity getRun(@PathVariable Long runId) {
        return testRepo.findById(runId).orElseThrow();
    }
}
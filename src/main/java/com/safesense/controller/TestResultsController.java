package com.safesense.controller;

import com.safesense.service.LiveTestRunner;
import com.safesense.service.TestResultService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestResultsController {

    private final TestResultService testResultService;
    private final LiveTestRunner liveTestRunner;

    public TestResultsController(TestResultService testResultService, LiveTestRunner liveTestRunner) {
        this.testResultService = testResultService;
        this.liveTestRunner = liveTestRunner;
    }

    @GetMapping("/api/test-results")
    public Map<String, Object> sample() {
        return testResultService.withSummary(testResultService.getSampleReport());
    }

    @PostMapping("/api/test-results/run")
    public Map<String, Object> runLive() {
        return testResultService.withSummary(liveTestRunner.run());
    }
}

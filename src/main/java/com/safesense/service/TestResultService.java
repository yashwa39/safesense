package com.safesense.service;

import com.safesense.model.TestCase;
import com.safesense.model.TestReport;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Loads the sample JSON once and computes summary counts from the file
 * (never hard-coded totals).
 */
@Service
public class TestResultService {

    private final TestReport sampleReport;

    public TestResultService() {
        this.sampleReport = TestReport.loadFromClasspath();
    }

    public TestReport getSampleReport() {
        return sampleReport;
    }

    public Map<String, Object> withSummary(TestReport report) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("project", report.getProject());
        body.put("build", report.getBuild());
        body.put("course", report.getCourse());
        body.put("team", report.getTeam());
        body.put("environment", report.getEnvironment());
        body.put("note", report.getNote());
        body.put("bug", report.getBug());
        body.put("tests", report.getTests());
        body.put("summary", summarise(report.getTests()));
        return body;
    }

    public Map<String, Object> summarise(List<TestCase> tests) {
        int total = tests == null ? 0 : tests.size();
        int passed = 0;
        int failed = 0;
        int fixed = 0;
        Map<String, Integer> byCategory = new LinkedHashMap<>();
        Map<String, Integer> byResult = new LinkedHashMap<>();
        if (tests != null) {
            for (TestCase test : tests) {
                String result = test.getResult() == null ? "" : test.getResult().toUpperCase(Locale.ROOT);
                switch (result) {
                    case "PASS" -> passed++;
                    case "FAIL" -> failed++;
                    case "FIXED" -> fixed++;
                    default -> {
                    }
                }
                byResult.merge(result.isBlank() ? "UNKNOWN" : result, 1, Integer::sum);
                String category = test.getCategory() == null ? "Other" : test.getCategory();
                byCategory.merge(category, 1, Integer::sum);
            }
        }
        double passRate = total == 0 ? 0 : ((passed + fixed) * 100.0) / total;
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("total", total);
        summary.put("passed", passed);
        summary.put("failed", failed);
        summary.put("fixed", fixed);
        summary.put("passRate", Math.round(passRate * 10.0) / 10.0);
        summary.put("byCategory", byCategory);
        summary.put("byResult", byResult);
        return summary;
    }
}

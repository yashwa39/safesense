package com.safesense;

import com.safesense.model.TestCase;
import com.safesense.model.TestReport;
import com.safesense.service.AuthService;
import com.safesense.service.LiveTestRunner;
import com.safesense.service.TestResultService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LiveTestRunnerTest {

    private TestReport report;
    private Map<String, TestCase> byId;

    @BeforeEach
    void runLive() {
        LiveTestRunner runner = new LiveTestRunner(new TestResultService(), new AuthService());
        report = runner.run();
        byId = report.getTests().stream().collect(Collectors.toMap(TestCase::getId, Function.identity()));
    }

    @Test
    void pptCasesU1ToU3Pass() {
        assertEquals("PASS", byId.get("U1").getResult());
        assertEquals("PASS", byId.get("U2").getResult());
        assertEquals("PASS", byId.get("U3").getResult());
        assertEquals(StatusSnippet.NORMAL, statusOf("U1"));
        assertEquals(StatusSnippet.CRITICAL, statusOf("U2"));
    }

    @Test
    void thresholdCasesT1ToT12PassAfterGreaterOrEqualFix() {
        Stream.of("T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12")
                .forEach(id -> {
                    TestCase test = byId.get(id);
                    assertNotNull(test, id);
                    assertEquals("PASS", test.getResult(), id + " actual=" + test.getActual());
                });
        assertEquals("CRITICAL", byId.get("T8").getActual());
        assertEquals("NORMAL", byId.get("T1").getActual());
        assertEquals("WARNING", byId.get("T2").getActual());
        assertEquals("CRITICAL", byId.get("T12").getActual());
    }

    private String statusOf(String id) {
        String actual = byId.get(id).getActual();
        return actual.contains(":") ? actual.substring(0, actual.indexOf(':')) : actual;
    }

    private static final class StatusSnippet {
        static final String NORMAL = "NORMAL";
        static final String CRITICAL = "CRITICAL";
    }
}

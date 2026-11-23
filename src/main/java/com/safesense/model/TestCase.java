package com.safesense.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * OOP: Encapsulation — one row in the sample or live test report.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestCase {

    private String id;
    private String category;
    private String name;
    private String input;
    private String expected;
    private String actual;
    private String oopConcept;
    private Integer durationMs;
    private String result;

    public TestCase() {
        // Jackson
    }

    public TestCase(String id, String category, String name, String input,
                    String expected, String actual, String oopConcept,
                    Integer durationMs, String result) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.input = input;
        this.expected = expected;
        this.actual = actual;
        this.oopConcept = oopConcept;
        this.durationMs = durationMs;
        this.result = result;
    }

    public static TestCase live(String id, String category, String name, String input,
                                String expected, String actual, String oopConcept, long durationMs) {
        boolean pass = expected.equalsIgnoreCase(actual)
                || actual.toUpperCase().startsWith(expected.toUpperCase())
                || actual.contains(expected);
        return new TestCase(id, category, name, input, expected, actual, oopConcept,
                (int) durationMs, pass ? "PASS" : "FAIL");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getExpected() {
        return expected;
    }

    public void setExpected(String expected) {
        this.expected = expected;
    }

    public String getActual() {
        return actual;
    }

    public void setActual(String actual) {
        this.actual = actual;
    }

    public String getOopConcept() {
        return oopConcept;
    }

    public void setOopConcept(String oopConcept) {
        this.oopConcept = oopConcept;
    }

    public Integer getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Integer durationMs) {
        this.durationMs = durationMs;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}

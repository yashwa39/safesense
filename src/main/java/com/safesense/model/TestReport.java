package com.safesense.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * OOP: Encapsulation — sample or live report loaded from JSON via a static factory.
 */
public class TestReport {

    private String project;
    private String build;
    private String course;
    private String team;
    private String environment;
    private String note;
    private BugReport bug;
    private List<TestCase> tests = new ArrayList<>();

    public TestReport() {
        // Jackson
    }

    public static TestReport loadFromClasspath() {
        try (InputStream in = TestReport.class.getResourceAsStream("/data/test-results.json")) {
            if (in == null) {
                throw new IllegalStateException("Missing classpath resource /data/test-results.json");
            }
            return new ObjectMapper().readValue(in, TestReport.class);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not load test-results.json", ex);
        }
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @JsonProperty("bug")
    public BugReport getBug() {
        return bug;
    }

    public void setBug(BugReport bug) {
        this.bug = bug;
    }

    public List<TestCase> getTests() {
        return tests;
    }

    public void setTests(List<TestCase> tests) {
        this.tests = tests == null ? new ArrayList<>() : tests;
    }
}

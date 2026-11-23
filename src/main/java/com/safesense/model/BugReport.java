package com.safesense.model;

/**
 * OOP: Encapsulation — BUG-01 captured from the T8 boundary failure.
 */
public class BugReport {

    private String id;
    private String symptom;
    private String rootCause;
    private String fix;
    private String retest;

    public BugReport() {
        // Jackson
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSymptom() {
        return symptom;
    }

    public void setSymptom(String symptom) {
        this.symptom = symptom;
    }

    public String getRootCause() {
        return rootCause;
    }

    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }

    public String getFix() {
        return fix;
    }

    public void setFix(String fix) {
        this.fix = fix;
    }

    public String getRetest() {
        return retest;
    }

    public void setRetest(String retest) {
        this.retest = retest;
    }
}

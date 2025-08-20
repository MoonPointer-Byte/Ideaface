package com.example.interview_agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Judge0Request {
    
    @JsonProperty("source_code")
    private String sourceCode;
    
    @JsonProperty("language_id")
    private String languageId;
    
    private String stdin;
    
    @JsonProperty("expected_output")
    private String expectedOutput;
    
    public Judge0Request() {}
    
    public Judge0Request(String sourceCode, String languageId, String stdin) {
        this.sourceCode = sourceCode;
        this.languageId = languageId;
        this.stdin = stdin;
    }
    
    public Judge0Request(String sourceCode, String languageId, String stdin, String expectedOutput) {
        this.sourceCode = sourceCode;
        this.languageId = languageId;
        this.stdin = stdin;
        this.expectedOutput = expectedOutput;
    }

    public String getSourceCode() {
        return sourceCode;
    }
    
    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }
    
    public String getLanguageId() {
        return languageId;
    }
    
    public void setLanguageId(String languageId) {
        this.languageId = languageId;
    }
    
    public String getStdin() {
        return stdin;
    }
    
    public void setStdin(String stdin) {
        this.stdin = stdin;
    }
    
    public String getExpectedOutput() {
        return expectedOutput;
    }
    
    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }
} 
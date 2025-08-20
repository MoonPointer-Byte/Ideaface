package com.example.interview_agent.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class CodeExecutionRequest {
    private String code;
    private String language;
    private Integer questionId;
    private String action;
    
    @JsonAlias("input")
    private String stdin;

    public CodeExecutionRequest() {}
    
    public CodeExecutionRequest(String code, String language, Integer questionId, String action) {
        this.code = code;
        this.language = language;
        this.questionId = questionId;
        this.action = action;
    }
    

    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public Integer getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getStdin() {
        return stdin;
    }
    
    public void setStdin(String stdin) {
        this.stdin = stdin;
    }
} 
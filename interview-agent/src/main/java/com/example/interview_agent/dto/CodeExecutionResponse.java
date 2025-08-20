package com.example.interview_agent.dto;

import java.util.List;

public class CodeExecutionResponse {
    private boolean success;
    private String message;
    private List<TestCaseResult> testCases;
    private int totalPassed;
    private int totalCases;
    private String executionTime;
    private String error;
    private boolean allPassed;
    
    public CodeExecutionResponse() {}
    
    public CodeExecutionResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<TestCaseResult> getTestCases() {
        return testCases;
    }
    
    public void setTestCases(List<TestCaseResult> testCases) {
        this.testCases = testCases;
    }
    
    public int getTotalPassed() {
        return totalPassed;
    }
    
    public void setTotalPassed(int totalPassed) {
        this.totalPassed = totalPassed;
    }
    
    public int getTotalCases() {
        return totalCases;
    }
    
    public void setTotalCases(int totalCases) {
        this.totalCases = totalCases;
    }
    
    public String getExecutionTime() {
        return executionTime;
    }
    
    public void setExecutionTime(String executionTime) {
        this.executionTime = executionTime;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public boolean isAllPassed() {
        return allPassed;
    }
    
    public void setAllPassed(boolean allPassed) {
        this.allPassed = allPassed;
    }
    
    public static class TestCaseResult {
        private int id;
        private String input;
        private String expectedOutput;
        private String actualOutput;
        private boolean passed;
        private String executionTime;
        
        public TestCaseResult() {}
        
        public TestCaseResult(int id, String input, String expectedOutput, 
                             String actualOutput, boolean passed, String executionTime) {
            this.id = id;
            this.input = input;
            this.expectedOutput = expectedOutput;
            this.actualOutput = actualOutput;
            this.passed = passed;
            this.executionTime = executionTime;
        }
        
        public int getId() {
            return id;
        }
        
        public void setId(int id) {
            this.id = id;
        }
        
        public String getInput() {
            return input;
        }
        
        public void setInput(String input) {
            this.input = input;
        }
        
        public String getExpectedOutput() {
            return expectedOutput;
        }
        
        public void setExpectedOutput(String expectedOutput) {
            this.expectedOutput = expectedOutput;
        }
        
        public String getActualOutput() {
            return actualOutput;
        }
        
        public void setActualOutput(String actualOutput) {
            this.actualOutput = actualOutput;
        }
        
        public boolean isPassed() {
            return passed;
        }
        
        public void setPassed(boolean passed) {
            this.passed = passed;
        }
        
        public String getExecutionTime() {
            return executionTime;
        }
        
        public void setExecutionTime(String executionTime) {
            this.executionTime = executionTime;
        }
    }
} 
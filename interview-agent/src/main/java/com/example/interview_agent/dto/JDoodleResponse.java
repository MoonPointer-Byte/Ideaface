package com.example.interview_agent.dto;

public class JDoodleResponse {
    private String output;
    private String statusCode;
    private String memory;
    private String cpuTime;
    private String error;
    private String stdout;
    private String stderr;
    private String time;
    

    public JDoodleResponse() {}
    

    public String getOutput() {
        return output;
    }
    
    public void setOutput(String output) {
        this.output = output;
    }
    
    public String getStatusCode() {
        return statusCode;
    }
    
    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }
    
    public String getMemory() {
        return memory;
    }
    
    public void setMemory(String memory) {
        this.memory = memory;
    }
    
    public String getCpuTime() {
        return cpuTime;
    }
    
    public void setCpuTime(String cpuTime) {
        this.cpuTime = cpuTime;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public String getStdout() {
        return stdout;
    }
    
    public void setStdout(String stdout) {
        this.stdout = stdout;
    }
    
    public String getStderr() {
        return stderr;
    }
    
    public void setStderr(String stderr) {
        this.stderr = stderr;
    }
    
    public String getTime() {
        return time;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
} 
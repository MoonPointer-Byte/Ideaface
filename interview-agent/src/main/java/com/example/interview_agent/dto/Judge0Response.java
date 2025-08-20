package com.example.interview_agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Judge0Response {
    
    private String stdout;
    private String stderr;
    
    @JsonProperty("compile_output")
    private String compileOutput;
    
    private String message;
    private Status status;
    private String time;
    private String memory;
    
    @JsonProperty("exit_code")
    private Integer exitCode;
    
    @JsonProperty("exit_signal")
    private String exitSignal;
    
    public Judge0Response() {}

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
    
    public String getCompileOutput() {
        return compileOutput;
    }
    
    public void setCompileOutput(String compileOutput) {
        this.compileOutput = compileOutput;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getTime() {
        return time;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
    
    public String getMemory() {
        return memory;
    }
    
    public void setMemory(String memory) {
        this.memory = memory;
    }
    
    public Integer getExitCode() {
        return exitCode;
    }
    
    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }
    
    public String getExitSignal() {
        return exitSignal;
    }
    
    public void setExitSignal(String exitSignal) {
        this.exitSignal = exitSignal;
    }
    
    public static class Status {
        private Integer id;
        private String description;
        
        public Status() {}
        
        public Integer getId() {
            return id;
        }
        
        public void setId(Integer id) {
            this.id = id;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
    }
} 
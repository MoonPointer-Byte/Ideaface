package com.example.interview_agent.dto;

public class JDoodleRequest {
    private String script;
    private String language;
    private String versionIndex;
    private String clientId;
    private String clientSecret;
    private String stdin;
    

    public JDoodleRequest() {}
    
    public JDoodleRequest(String script, String language, String versionIndex, 
                         String clientId, String clientSecret, String stdin) {
        this.script = script;
        this.language = language;
        this.versionIndex = versionIndex;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.stdin = stdin;
    }
    

    public String getScript() {
        return script;
    }
    
    public void setScript(String script) {
        this.script = script;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getVersionIndex() {
        return versionIndex;
    }
    
    public void setVersionIndex(String versionIndex) {
        this.versionIndex = versionIndex;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public String getClientSecret() {
        return clientSecret;
    }
    
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
    
    public String getStdin() {
        return stdin;
    }
    
    public void setStdin(String stdin) {
        this.stdin = stdin;
    }
} 
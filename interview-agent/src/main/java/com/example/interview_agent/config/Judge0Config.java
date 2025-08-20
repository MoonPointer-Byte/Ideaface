package com.example.interview_agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "judge0.api")
public class Judge0Config {
    
    private String url;
    private String rapidApiKey;
    private String rapidApiHost;
    
    // Getters and Setters
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getRapidApiKey() {
        return rapidApiKey;
    }
    
    public void setRapidApiKey(String rapidApiKey) {
        this.rapidApiKey = rapidApiKey;
    }
    
    public String getRapidApiHost() {
        return rapidApiHost;
    }
    
    public void setRapidApiHost(String rapidApiHost) {
        this.rapidApiHost = rapidApiHost;
    }
} 
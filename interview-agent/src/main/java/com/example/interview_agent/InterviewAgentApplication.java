package com.example.interview_agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync; // 1. 导入注解

@SpringBootApplication
@EnableAsync
public class InterviewAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(InterviewAgentApplication.class, args);
	}

}
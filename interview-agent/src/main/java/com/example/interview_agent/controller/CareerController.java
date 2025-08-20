package com.example.interview_agent.controller;

import com.example.interview_agent.dto.CareerPlanRequest;
import com.example.interview_agent.service.CareerDevelopmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/career")
public class CareerController {

    @Autowired
    private CareerDevelopmentService careerDevelopmentService;

    @PostMapping("/plan")
    public ResponseEntity<?> generateCareerPlan(@RequestBody CareerPlanRequest request) {
        try {

            String plan = careerDevelopmentService.generatePlan(request.getCurrentSkills(), request.getTargetPositionId());
            return ResponseEntity.ok(Map.of("plan", plan));

        } catch (Exception e) {



            Map<String, String> errorResponse = Map.of(
                    "error", "服务器在处理您的请求时发生内部错误。",
                    "message", e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
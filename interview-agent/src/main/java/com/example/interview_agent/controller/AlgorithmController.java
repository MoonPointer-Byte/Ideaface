package com.example.interview_agent.controller;

import com.example.interview_agent.dto.CodeExecutionRequest;
import com.example.interview_agent.dto.CodeExecutionResponse;
import com.example.interview_agent.dto.Judge0Response;
import com.example.interview_agent.service.AlgorithmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/algorithm")
public class AlgorithmController {
    
    @Autowired
    private AlgorithmService algorithmService;
    
    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeCode(@RequestBody CodeExecutionRequest request) {
        try {

            Judge0Response judge0Response = algorithmService.executeCodeDirect(
                request.getCode(), 
                request.getLanguage(), 
                request.getStdin() != null ? request.getStdin() : ""
            );
            

            Map<String, Object> result = new HashMap<>();
            result.put("output", judge0Response.getStdout());
            result.put("stderr", judge0Response.getStderr());
            result.put("status", judge0Response.getStatus() != null ? 
                judge0Response.getStatus().getDescription() : "未知状态");
            result.put("success", judge0Response.getStatus() != null && 
                judge0Response.getStatus().getId() == 3); // 3表示Accepted
            result.put("time", judge0Response.getTime());
            result.put("compile_output", judge0Response.getCompileOutput());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "系统错误: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @PostMapping("/test")
    public ResponseEntity<CodeExecutionResponse> testCode(@RequestBody CodeExecutionRequest request) {
        request.setAction("test");
        try {
            CodeExecutionResponse response = algorithmService.runCode(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            CodeExecutionResponse errorResponse = new CodeExecutionResponse(false, "系统错误: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @PostMapping("/submit")
    public ResponseEntity<CodeExecutionResponse> submitCode(@RequestBody CodeExecutionRequest request) {
        request.setAction("submit");
        try {
            CodeExecutionResponse response = algorithmService.runCode(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            CodeExecutionResponse errorResponse = new CodeExecutionResponse(false, "系统错误: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/test-connection")
    public ResponseEntity<String> testJudge0Connection() {
        try {

            Judge0Response response = algorithmService.executeCodeDirect(
                "console.log('Hello, Judge0!');", 
                "javascript", 
                ""
            );
            
            if (response.getStatus() != null && response.getStatus().getId() == 3) {
                return ResponseEntity.ok("Judge0 API连接成功");
            } else {
                String statusDesc = response.getStatus() != null ? 
                    response.getStatus().getDescription() : "未知状态";
                return ResponseEntity.ok("Judge0 API连接失败: " + statusDesc);
            }
        } catch (Exception e) {
            return ResponseEntity.ok("Judge0 API连接异常: " + e.getMessage());
        }
    }
} 
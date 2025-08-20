package com.example.interview_agent.service;

import com.example.interview_agent.config.Judge0Config;
import com.example.interview_agent.dto.*;
import com.example.interview_agent.util.LanguageMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AlgorithmService {
    
    @Autowired
    private Judge0Config judge0Config;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public CodeExecutionResponse runCode(CodeExecutionRequest request) {
        try {
            if ("test".equals(request.getAction())) {
                return runTestCases(request);
            } else if ("submit".equals(request.getAction())) {
                return submitCode(request);
            } else {
                return new CodeExecutionResponse(false, "无效的操作类型");
            }
        } catch (Exception e) {
            return new CodeExecutionResponse(false, "代码执行失败: " + e.getMessage());
        }
    }

    public Judge0Response executeCodeDirect(String code, String language, String input) throws Exception {
        // 检查语言是否支持
        if (!LanguageMapper.isSupported(language)) {
            throw new IllegalArgumentException("不支持的编程语言: " + language);
        }
        
        String languageId = LanguageMapper.getLanguageId(language);
        
        // 创建 Judge0 请求
        Judge0Request judge0Request = new Judge0Request(code, languageId, input);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-RapidAPI-Key", judge0Config.getRapidApiKey());
        headers.set("X-RapidAPI-Host", judge0Config.getRapidApiHost());
        
        HttpEntity<Judge0Request> entity = new HttpEntity<>(judge0Request, headers);
        
        ResponseEntity<Judge0Response> responseEntity = restTemplate.postForEntity(
            judge0Config.getUrl(),
            entity,
            Judge0Response.class
        );
        
        return responseEntity.getBody();
    }
    
    private CodeExecutionResponse runTestCases(CodeExecutionRequest request) {
        // 获取测试用例（从题目示例中提取）
        List<TestCase> testCases = getTestCasesForQuestion(request.getQuestionId());
        
        CodeExecutionResponse response = new CodeExecutionResponse();
        List<CodeExecutionResponse.TestCaseResult> results = new ArrayList<>();
        
        int passedCount = 0;
        long totalExecutionTime = 0;
        
        for (int i = 0; i < testCases.size(); i++) {
            TestCase testCase = testCases.get(i);
            
            try {
                Judge0Response judge0Response = executeCode(
                    request.getCode(), 
                    request.getLanguage(), 
                    testCase.getInput()
                );
                
                String actualOutput = cleanOutput(judge0Response.getStdout());
                String expectedOutput = cleanOutput(testCase.getExpectedOutput());
                boolean passed = actualOutput.equals(expectedOutput);
                
                if (passed) {
                    passedCount++;
                }
                
                String executionTime = judge0Response.getTime();
                if (executionTime == null) {
                    executionTime = "0";
                }
                if (!executionTime.endsWith("ms")) {
                    executionTime += "ms";
                }
                
                // 如果有编译错误或运行时错误，显示错误信息
                String actualOutputWithError = actualOutput;
                if (judge0Response.getStderr() != null && !judge0Response.getStderr().trim().isEmpty()) {
                    actualOutputWithError += "\n错误输出: " + judge0Response.getStderr();
                }
                if (judge0Response.getCompileOutput() != null && !judge0Response.getCompileOutput().trim().isEmpty()) {
                    actualOutputWithError += "\n编译输出: " + judge0Response.getCompileOutput();
                }
                
                results.add(new CodeExecutionResponse.TestCaseResult(
                    i + 1,
                    testCase.getInput(),
                    testCase.getExpectedOutput(),
                    actualOutputWithError,
                    passed,
                    executionTime
                ));
                
                // 累计执行时间
                try {
                    String timeStr = executionTime.replace("ms", "").trim();
                    totalExecutionTime += Long.parseLong(timeStr);
                } catch (NumberFormatException e) {
                    // 如果解析失败，使用默认值
                    totalExecutionTime += 50;
                }
                
            } catch (Exception e) {
                results.add(new CodeExecutionResponse.TestCaseResult(
                    i + 1,
                    testCase.getInput(),
                    testCase.getExpectedOutput(),
                    "执行错误: " + e.getMessage(),
                    false,
                    "0ms"
                ));
            }
        }
        
        response.setSuccess(true);
        response.setTestCases(results);
        response.setTotalPassed(passedCount);
        response.setTotalCases(testCases.size());
        response.setAllPassed(passedCount == testCases.size());
        response.setExecutionTime(totalExecutionTime + "ms");
        response.setMessage("测试完成");
        
        return response;
    }
    
    private CodeExecutionResponse submitCode(CodeExecutionRequest request) {
        try {
            // 如果用户提供了自定义输入，使用自定义输入
            String input = request.getStdin() != null ? request.getStdin() : "";
            
            Judge0Response judge0Response = executeCode(
                request.getCode(), 
                request.getLanguage(), 
                input
            );
            
            CodeExecutionResponse response = new CodeExecutionResponse();
            
            // 判断提交是否成功（基于 Judge0 状态）
            boolean success = judge0Response.getStatus() != null && 
                             judge0Response.getStatus().getId() == 3; // 3 表示 Accepted
            
            String executionTime = judge0Response.getTime();
            if (executionTime == null) {
                executionTime = "0";
            }
            if (!executionTime.endsWith("ms")) {
                executionTime += "ms";
            }
            
            response.setSuccess(success);
            response.setAllPassed(success);
            response.setExecutionTime(executionTime);
            
            // 构建输出信息
            StringBuilder output = new StringBuilder();
            if (judge0Response.getStdout() != null && !judge0Response.getStdout().trim().isEmpty()) {
                output.append("标准输出:\n").append(judge0Response.getStdout());
            }
            if (judge0Response.getStderr() != null && !judge0Response.getStderr().trim().isEmpty()) {
                output.append("\n错误输出:\n").append(judge0Response.getStderr());
            }
            if (judge0Response.getCompileOutput() != null && !judge0Response.getCompileOutput().trim().isEmpty()) {
                output.append("\n编译输出:\n").append(judge0Response.getCompileOutput());
            }
            
            String statusDescription = judge0Response.getStatus() != null ? 
                judge0Response.getStatus().getDescription() : "未知状态";
            
            response.setMessage(success ? 
                "代码执行成功！状态: " + statusDescription : 
                "代码执行失败，状态: " + statusDescription);
            
            if (!success && output.length() > 0) {
                response.setError(output.toString());
            }
            
            return response;
            
        } catch (Exception e) {
            return new CodeExecutionResponse(false, "提交失败: " + e.getMessage());
        }
    }
    
    private Judge0Response executeCode(String code, String language, String input) throws Exception {
        // 检查语言是否支持
        if (!LanguageMapper.isSupported(language)) {
            throw new IllegalArgumentException("不支持的编程语言: " + language);
        }
        
        String languageId = LanguageMapper.getLanguageId(language);
        
        // 创建 Judge0 请求
        Judge0Request judge0Request = new Judge0Request(code, languageId, input);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-RapidAPI-Key", judge0Config.getRapidApiKey());
        headers.set("X-RapidAPI-Host", judge0Config.getRapidApiHost());
        
        HttpEntity<Judge0Request> entity = new HttpEntity<>(judge0Request, headers);
        
        ResponseEntity<Judge0Response> responseEntity = restTemplate.postForEntity(
            judge0Config.getUrl(),
            entity,
            Judge0Response.class
        );
        
        return responseEntity.getBody();
    }
    
    private String cleanOutput(String output) {
        if (output == null) return "";
        return output.trim();
    }
    
    private List<TestCase> getTestCasesForQuestion(Integer questionId) {
        // 这里应该从数据库或题目文件中获取测试用例
        // 现在用硬编码的示例数据
        List<TestCase> testCases = new ArrayList<>();
        
        switch (questionId) {
            case 1: // 反转链表
                testCases.add(new TestCase("1 2 3", "3 2 1"));
                testCases.add(new TestCase("", ""));
                break;
            case 2: // 链表内指定区间反转
                testCases.add(new TestCase("1 2 3 4 5\n2 4", "1 4 3 2 5"));
                testCases.add(new TestCase("5\n1 1", "5"));
                break;
            case 3: // 合并两个排序的链表
                testCases.add(new TestCase("1 3 5\n2 4 6", "1 2 3 4 5 6"));
                testCases.add(new TestCase("\n", ""));
                testCases.add(new TestCase("-1 2 4\n1 3 4", "-1 1 2 3 4 4"));
                break;
            default:
                testCases.add(new TestCase("Hello World", "Hello World"));
                break;
        }
        
        return testCases;
    }
    
    // 内部类：测试用例
    private static class TestCase {
        private String input;
        private String expectedOutput;
        
        public TestCase(String input, String expectedOutput) {
            this.input = input;
            this.expectedOutput = expectedOutput;
        }
        
        public String getInput() {
            return input;
        }
        
        public String getExpectedOutput() {
            return expectedOutput;
        }
    }
} 
package com.example.interview_agent.controller;

import com.example.interview_agent.entity.Question;
import com.example.interview_agent.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @GetMapping
    public ResponseEntity<Page<Question>> searchQuestions(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String title,
            Pageable pageable) {

        Page<Question> questionsPage = questionService.searchQuestions(category, title, pageable);
        return ResponseEntity.ok(questionsPage);
    }
}
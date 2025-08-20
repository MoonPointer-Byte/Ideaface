package com.example.interview_agent.controller;

import com.example.interview_agent.dto.PageResponse;
import com.example.interview_agent.dto.QuestionDto;
import com.example.interview_agent.entity.Course;
import com.example.interview_agent.entity.LearningPath;
import com.example.interview_agent.entity.Question;
import com.example.interview_agent.repository.CourseRepository;
import com.example.interview_agent.repository.LearningPathRepository;
import com.example.interview_agent.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/content")
public class ContentController {

    @Autowired private QuestionRepository questionRepository;
    @Autowired private LearningPathRepository learningPathRepository;
    @Autowired private CourseRepository courseRepository;

    @GetMapping("/questions")
    public ResponseEntity<PageResponse<QuestionDto>> getQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "") String keyword // 统一用keyword
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        Page<Question> questionPage;

        try {
            Long id = Long.parseLong(keyword);
            questionPage = questionRepository.findById(id, pageable);
            Page<QuestionDto> dtoPage = questionPage.map(QuestionDto::fromEntity);
            return ResponseEntity.ok(new PageResponse<>(dtoPage));
        } catch (NumberFormatException e) {

            if (StringUtils.hasText(category)) {
                questionPage = questionRepository.findByCategoryAndTitleContainingIgnoreCase(category, keyword, pageable);
            } else {
                questionPage = questionRepository.findByTitleContainingIgnoreCase(keyword, pageable);
            }
        }

        Page<QuestionDto> dtoPage = questionPage.map(QuestionDto::fromEntity);
        return ResponseEntity.ok(new PageResponse<>(dtoPage));
    }

    @GetMapping("/questions/{id}")
    public ResponseEntity<QuestionDto> getQuestionById(@PathVariable Long id) {
        return questionRepository.findById(id)
                .map(QuestionDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/learning-paths")
    public ResponseEntity<List<LearningPath>> getLearningPaths() {
        return ResponseEntity.ok(learningPathRepository.findAll());
    }

    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getCourses() {
        return ResponseEntity.ok(courseRepository.findAll());
    }
}
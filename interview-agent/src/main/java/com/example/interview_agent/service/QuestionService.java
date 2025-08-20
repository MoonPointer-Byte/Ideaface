package com.example.interview_agent.service;

import com.example.interview_agent.entity.Question;
import com.example.interview_agent.repository.QuestionRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page; // <-- 导入 Page
import org.springframework.data.domain.Pageable; // <-- 导入 Pageable
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils; // 导入 StringUtils
import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    public Page<Question> searchQuestions(String category, String title, Pageable pageable) {

        Specification<Question> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(category)) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }


            if (StringUtils.hasText(title)) {

                predicates.add(criteriaBuilder.like(root.get("title"), "%" + title + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return questionRepository.findAll(spec, pageable);
    }
}
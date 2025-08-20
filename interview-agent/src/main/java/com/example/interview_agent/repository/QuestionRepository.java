package com.example.interview_agent.repository;

import com.example.interview_agent.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long>, JpaSpecificationExecutor<Question> { // <-- 2. 在这里同时继承它

    Page<Question> findByCategoryAndTitleContaining(String category, String title, Pageable pageable);
    Page<Question> findByTitleContaining(String title, Pageable pageable);

    Page<Question> findByCategoryAndTitleContainingIgnoreCase(String category, String title, Pageable pageable);

    Page<Question> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Question> findById(Long id, Pageable pageable);
}

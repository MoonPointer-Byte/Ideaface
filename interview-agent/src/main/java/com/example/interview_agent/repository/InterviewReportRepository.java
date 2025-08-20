package com.example.interview_agent.repository;

import com.example.interview_agent.entity.InterviewReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // 如果需要自定义查询返回列表


@Repository
public interface InterviewReportRepository extends JpaRepository<InterviewReport, String> {




    List<InterviewReport> findByUserIdOrderByCreateTimeDesc(String userId);

    List<InterviewReport>findBySessionId(String sessionId);

}
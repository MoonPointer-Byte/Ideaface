package com.example.interview_agent.repository;

import com.example.interview_agent.service.InterviewSession;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


@Repository
public class InterviewSessionRepository {

    private final ConcurrentHashMap<String, InterviewSession> sessions = new ConcurrentHashMap<>();


    public void save(InterviewSession session) {
        sessions.put(session.getSessionId(), session);
    }


    public Optional<InterviewSession> findById(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }


    public void deleteById(String sessionId) {
        sessions.remove(sessionId);
    }
}
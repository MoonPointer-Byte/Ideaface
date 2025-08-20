package com.example.interview_agent.dto;

import com.example.interview_agent.entity.Question;
import java.util.Objects;


public class QuestionDto {

    private Long id;
    private String title;
    private String content;
    private String category;
    private String difficulty;


    public QuestionDto() {
    }

    public QuestionDto(Long id, String title, String content, String category, String difficulty) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;
        this.difficulty = difficulty;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public static QuestionDto fromEntity(Question question) {
        if (question == null) {
            return null;
        }

        QuestionDto dto = new QuestionDto();

        dto.setId(question.getId());
        dto.setTitle(question.getTitle());
        dto.setContent(question.getContent());
        dto.setCategory(question.getCategory());
        dto.setDifficulty(question.getDifficulty());

        return dto;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionDto that = (QuestionDto) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(title, that.title) &&
                Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, category);
    }

    @Override
    public String toString() {
        return "QuestionDto{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", difficulty='" + difficulty + '\'' +
                '}';
    }
}
package com.example.interview_agent.repository;

import com.example.interview_agent.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.Set;
import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    // 根据名称查找标签，用于避免创建重复标签
    Optional<Tag> findByName(String name);

    // 根据一组名称查找已存在的标签
    Set<Tag> findByNameIn(List<String> names);
}
package com.example.interview_agent.repository;

import com.example.interview_agent.entity.PostComment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    // 获取某篇文章下的所有【顶级】评论（即 parent_id 为 NULL 的评论）
    // @EntityGraph 会一次性抓取 user 和 replies 信息，避免 N+1 查询
    @EntityGraph(attributePaths = {"user", "replies", "replies.user"})
    List<PostComment> findByPostIdAndParentIsNullOrderByCreateTimeDesc(Long postId);
}
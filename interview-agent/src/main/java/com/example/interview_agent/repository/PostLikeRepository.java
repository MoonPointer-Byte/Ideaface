package com.example.interview_agent.repository;

import com.example.interview_agent.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    // 根据用户ID和文章ID查找点赞记录
    Optional<PostLike> findByUserIdAndPostId(Long userId, Long postId);
}
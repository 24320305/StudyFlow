package com.studyflow.backend.community;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    long countByPostId(Long postId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    long deleteByUserIdAndPostId(Long userId, Long postId);
}

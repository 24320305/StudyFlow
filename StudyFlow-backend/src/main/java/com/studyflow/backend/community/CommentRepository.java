package com.studyflow.backend.community;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    long countByPostIdAndStatus(Long postId, CommentStatus status);

    List<Comment> findAllByPostIdAndStatusOrderByCreatedAtAsc(Long postId, CommentStatus status);

    Optional<Comment> findByIdAndUserId(Long id, Long userId);
}

package com.studyflow.backend.community;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.studyflow.backend.user.UserStatus;

public interface PostRepository extends JpaRepository<Post, Long> {

    boolean existsByCheckInId(Long checkInId);

    Optional<Post> findByCheckInId(Long checkInId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT post FROM Post post JOIN FETCH post.user WHERE post.id = :id")
    Optional<Post> findByIdForInteraction(@Param("id") Long id);

    Page<Post> findAllByVisibilityAndStatusAndUser_Status(
            PostVisibility visibility,
            PostStatus status,
            UserStatus userStatus,
            Pageable pageable);

    Page<Post> findAllByVisibilityAndStatusAndUser_StatusAndContentContainingIgnoreCase(
            PostVisibility visibility,
            PostStatus status,
            UserStatus userStatus,
            String content,
            Pageable pageable);

    Page<Post> findAllByUserIdAndStatusNot(Long userId, PostStatus status, Pageable pageable);
}

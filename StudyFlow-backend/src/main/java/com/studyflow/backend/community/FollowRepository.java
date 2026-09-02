package com.studyflow.backend.community;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<FollowRelation, Long> {

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    long countByFollowerIdAndFollowingId(Long followerId, Long followingId);

    long deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);
}

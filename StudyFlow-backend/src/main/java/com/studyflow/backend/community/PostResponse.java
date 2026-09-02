package com.studyflow.backend.community;

import java.time.OffsetDateTime;

public record PostResponse(
        Long id,
        CommunityUserResponse author,
        Long checkInId,
        String content,
        PostVisibility visibility,
        PostStatus status,
        long likeCount,
        long commentCount,
        boolean likedByCurrentUser,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}

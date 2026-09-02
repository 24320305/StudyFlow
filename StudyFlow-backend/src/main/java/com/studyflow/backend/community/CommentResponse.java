package com.studyflow.backend.community;

import java.time.OffsetDateTime;

public record CommentResponse(
        Long id,
        CommunityUserResponse author,
        String content,
        CommentStatus status,
        OffsetDateTime createdAt) {
}

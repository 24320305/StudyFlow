package com.studyflow.backend.community;

public record LikeStateResponse(Long postId, boolean liked, long likeCount) {
}

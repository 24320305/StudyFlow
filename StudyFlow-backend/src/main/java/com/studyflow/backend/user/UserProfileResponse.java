package com.studyflow.backend.user;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public record UserProfileResponse(
        Long id,
        String email,
        String nickname,
        String avatarUrl,
        UserRole role,
        UserStatus status,
        OffsetDateTime createdAt) {

    public static UserProfileResponse from(UserAccount user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getAvatarUrl(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt() == null ? null : OffsetDateTime.ofInstant(user.getCreatedAt(), ZoneId.of("Asia/Shanghai")));
    }
}

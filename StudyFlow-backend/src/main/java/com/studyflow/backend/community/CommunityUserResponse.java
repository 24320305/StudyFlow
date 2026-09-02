package com.studyflow.backend.community;

import com.studyflow.backend.user.UserAccount;

public record CommunityUserResponse(Long id, String nickname, String avatarUrl) {

    public static CommunityUserResponse from(UserAccount user) {
        return new CommunityUserResponse(user.getId(), user.getNickname(), user.getAvatarUrl());
    }
}

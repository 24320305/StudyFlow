package com.studyflow.backend.user;

import java.time.OffsetDateTime;

public record AuthTokenResponse(
        String accessToken,
        String tokenType,
        OffsetDateTime expiresAt,
        UserProfileResponse user) {
}

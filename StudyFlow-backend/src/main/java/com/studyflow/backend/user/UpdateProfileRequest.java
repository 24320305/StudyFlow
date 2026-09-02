package com.studyflow.backend.user;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 1, max = 80) String nickname,
        @Size(max = 500) String avatarUrl) {
}

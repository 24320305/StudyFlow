package com.studyflow.backend.community;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
        @NotNull Long checkInId,
        @NotBlank @Size(max = 500) String content,
        @NotNull PostVisibility visibility) {
}

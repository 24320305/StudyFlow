package com.studyflow.backend.community;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePostRequest(
        @Size(min = 1, max = 500) @Pattern(regexp = "(?s).*\\S.*", message = "must not be blank") String content,
        PostVisibility visibility) {
}

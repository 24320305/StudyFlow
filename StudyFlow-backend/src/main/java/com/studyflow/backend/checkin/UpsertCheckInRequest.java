package com.studyflow.backend.checkin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpsertCheckInRequest(
        @NotNull @Min(0) Integer durationMinutes,
        @NotNull Boolean completed,
        @Size(max = 500) String note,
        @Size(max = 500) String imageUrl) {
}

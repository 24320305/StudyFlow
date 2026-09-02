package com.studyflow.backend.plan;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePlanRequest(
        @Size(min = 1, max = 120) @Pattern(regexp = "(?s).*\\S.*", message = "must not be blank") String name,
        LocalDate startDate,
        LocalDate endDate,
        @Min(1) Integer dailyTarget,
        PlanStatus status) {
}

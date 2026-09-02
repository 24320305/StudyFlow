package com.studyflow.backend.plan;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public record StudyPlanResponse(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        Integer dailyTarget,
        PlanStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public static StudyPlanResponse from(StudyPlan plan) {
        return new StudyPlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getStartDate(),
                plan.getEndDate(),
                plan.getDailyTarget(),
                plan.getStatus(),
                plan.getCreatedAt() == null ? null : OffsetDateTime.ofInstant(plan.getCreatedAt(), ZoneId.of("Asia/Shanghai")),
                plan.getUpdatedAt() == null ? null : OffsetDateTime.ofInstant(plan.getUpdatedAt(), ZoneId.of("Asia/Shanghai")));
    }
}

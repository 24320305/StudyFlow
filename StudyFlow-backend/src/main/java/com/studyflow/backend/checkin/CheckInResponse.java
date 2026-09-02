package com.studyflow.backend.checkin;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public record CheckInResponse(
        Long id,
        Long planId,
        java.time.LocalDate checkDate,
        Integer durationMinutes,
        boolean completed,
        String note,
        String imageUrl,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public static CheckInResponse from(CheckIn checkIn) {
        return new CheckInResponse(
                checkIn.getId(),
                checkIn.getPlan().getId(),
                checkIn.getCheckDate(),
                checkIn.getDurationMinutes(),
                checkIn.isCompleted(),
                checkIn.getNote(),
                checkIn.getImageUrl(),
                checkIn.getCreatedAt() == null ? null : OffsetDateTime.ofInstant(checkIn.getCreatedAt(), ZoneId.of("Asia/Shanghai")),
                checkIn.getUpdatedAt() == null ? null : OffsetDateTime.ofInstant(checkIn.getUpdatedAt(), ZoneId.of("Asia/Shanghai")));
    }
}

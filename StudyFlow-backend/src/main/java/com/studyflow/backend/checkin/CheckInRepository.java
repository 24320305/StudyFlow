package com.studyflow.backend.checkin;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    Optional<CheckIn> findByIdAndUserId(Long id, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT checkIn FROM CheckIn checkIn WHERE checkIn.id = :id AND checkIn.user.id = :userId")
    Optional<CheckIn> findByIdAndUserIdForPublication(@Param("id") Long id, @Param("userId") Long userId);

    Optional<CheckIn> findByUserIdAndPlanIdAndCheckDate(Long userId, Long planId, LocalDate checkDate);

    List<CheckIn> findAllByUserIdAndPlanIdOrderByCheckDateDescCreatedAtDesc(Long userId, Long planId);
}

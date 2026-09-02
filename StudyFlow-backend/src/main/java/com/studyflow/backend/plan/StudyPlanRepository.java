package com.studyflow.backend.plan;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {

    Page<StudyPlan> findAllByUserId(Long userId, Pageable pageable);

    Page<StudyPlan> findAllByUserIdAndStatus(Long userId, PlanStatus status, Pageable pageable);

    Optional<StudyPlan> findByIdAndUserId(Long id, Long userId);
}

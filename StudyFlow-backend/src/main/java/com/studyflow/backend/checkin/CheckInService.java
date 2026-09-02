package com.studyflow.backend.checkin;

import java.time.LocalDate;
import java.util.List;

import com.studyflow.backend.common.exception.BusinessException;
import com.studyflow.backend.plan.PlanStatus;
import com.studyflow.backend.plan.StudyPlan;
import com.studyflow.backend.plan.StudyPlanService;
import com.studyflow.backend.user.UserAccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final StudyPlanService studyPlanService;
    private final UserAccountService userAccountService;

    public CheckInService(
            CheckInRepository checkInRepository,
            StudyPlanService studyPlanService,
            UserAccountService userAccountService) {
        this.checkInRepository = checkInRepository;
        this.studyPlanService = studyPlanService;
        this.userAccountService = userAccountService;
    }

    @Transactional
    public CheckInResponse upsert(Long userId, Long planId, LocalDate checkDate, UpsertCheckInRequest request) {
        StudyPlan plan = studyPlanService.getOwnedPlan(userId, planId);
        if (plan.getStatus() != PlanStatus.ACTIVE) {
            throw BusinessException.conflict("PLAN_NOT_ACTIVE", "Only an active plan can receive a check-in");
        }
        if (checkDate.isBefore(plan.getStartDate()) || checkDate.isAfter(plan.getEndDate())) {
            throw BusinessException.badRequest("CHECK_IN_DATE_OUT_OF_RANGE", "checkDate must be within the plan date range");
        }

        CheckIn checkIn = checkInRepository.findByUserIdAndPlanIdAndCheckDate(userId, planId, checkDate)
                .orElseGet(() -> new CheckIn(
                        userAccountService.getRequired(userId),
                        plan,
                        checkDate,
                        request.durationMinutes(),
                        request.completed(),
                        trimToNull(request.note()),
                        trimToNull(request.imageUrl())));
        checkIn.update(
                request.durationMinutes(),
                request.completed(),
                trimToNull(request.note()),
                trimToNull(request.imageUrl()));
        return CheckInResponse.from(checkInRepository.saveAndFlush(checkIn));
    }

    public List<CheckInResponse> list(Long userId, Long planId) {
        studyPlanService.getOwnedPlan(userId, planId);
        return checkInRepository.findAllByUserIdAndPlanIdOrderByCheckDateDescCreatedAtDesc(userId, planId).stream()
                .map(CheckInResponse::from)
                .toList();
    }

    public CheckIn getOwnedCheckIn(Long userId, Long checkInId) {
        return checkInRepository.findByIdAndUserId(checkInId, userId)
                .orElseThrow(() -> BusinessException.notFound("CHECK_IN_NOT_FOUND", "Check-in was not found"));
    }

    /**
     * Serializes publishing requests for one check-in so a double click cannot
     * create two posts before the unique database constraint is evaluated.
     */
    public CheckIn getOwnedCheckInForPublication(Long userId, Long checkInId) {
        return checkInRepository.findByIdAndUserIdForPublication(checkInId, userId)
                .orElseThrow(() -> BusinessException.notFound("CHECK_IN_NOT_FOUND", "Check-in was not found"));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

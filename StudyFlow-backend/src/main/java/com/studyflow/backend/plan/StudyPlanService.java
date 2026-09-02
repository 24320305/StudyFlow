package com.studyflow.backend.plan;

import java.time.LocalDate;

import com.studyflow.backend.common.api.PageResponse;
import com.studyflow.backend.common.exception.BusinessException;
import com.studyflow.backend.user.UserAccount;
import com.studyflow.backend.user.UserAccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
@Transactional(readOnly = true)
public class StudyPlanService {

    private final StudyPlanRepository studyPlanRepository;
    private final UserAccountService userAccountService;

    public StudyPlanService(StudyPlanRepository studyPlanRepository, UserAccountService userAccountService) {
        this.studyPlanRepository = studyPlanRepository;
        this.userAccountService = userAccountService;
    }

    @Transactional
    public StudyPlanResponse create(Long userId, CreatePlanRequest request) {
        validateDates(request.startDate(), request.endDate());
        validatePlanName(request.name());
        UserAccount user = userAccountService.getRequired(userId);
        StudyPlan plan = new StudyPlan(
                user,
                request.name().trim(),
                request.startDate(),
                request.endDate(),
                request.dailyTarget());
        return StudyPlanResponse.from(studyPlanRepository.save(plan));
    }

    public PageResponse<StudyPlanResponse> list(Long userId, int page, int pageSize) {
        return list(userId, page, pageSize, null);
    }

    public PageResponse<StudyPlanResponse> list(Long userId, int page, int pageSize, PlanStatus status) {
        if (page < 1 || pageSize < 1 || pageSize > 100) {
            throw BusinessException.badRequest("INVALID_PAGE", "page must be >= 1 and pageSize must be between 1 and 100");
        }
        var pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = status == null
                ? studyPlanRepository.findAllByUserId(userId, pageable)
                : studyPlanRepository.findAllByUserIdAndStatus(userId, status, pageable);
        return new PageResponse<>(
                result.getContent().stream().map(StudyPlanResponse::from).toList(),
                page,
                pageSize,
                result.getTotalElements());
    }

    public StudyPlanResponse get(Long userId, Long planId) {
        return StudyPlanResponse.from(getOwnedPlan(userId, planId));
    }

    @Transactional
    public StudyPlanResponse update(Long userId, Long planId, UpdatePlanRequest request) {
        if (request.name() == null && request.startDate() == null && request.endDate() == null
                && request.dailyTarget() == null && request.status() == null) {
            throw BusinessException.badRequest("EMPTY_PLAN_UPDATE", "Provide at least one plan field to update");
        }
        StudyPlan plan = getOwnedPlan(userId, planId);
        if (request.name() != null) {
            validatePlanName(request.name());
        }
        LocalDate startDate = request.startDate() == null ? plan.getStartDate() : request.startDate();
        LocalDate endDate = request.endDate() == null ? plan.getEndDate() : request.endDate();
        validateDates(startDate, endDate);
        plan.update(
                request.name() == null ? null : request.name().trim(),
                request.startDate(),
                request.endDate(),
                request.dailyTarget());
        if (request.status() != null) {
            validateStatusTransition(plan.getStatus(), request.status());
            plan.changeStatus(request.status());
        }
        return StudyPlanResponse.from(studyPlanRepository.saveAndFlush(plan));
    }

    @Transactional
    public void delete(Long userId, Long planId) {
        StudyPlan plan = getOwnedPlan(userId, planId);
        studyPlanRepository.delete(plan);
    }

    public StudyPlan getOwnedPlan(Long userId, Long planId) {
        return studyPlanRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> BusinessException.notFound("PLAN_NOT_FOUND", "Study plan was not found"));
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw BusinessException.badRequest("INVALID_PLAN_DATES", "endDate cannot be earlier than startDate");
        }
    }

    private void validatePlanName(String name) {
        if (name == null || name.isBlank()) {
            throw BusinessException.badRequest("INVALID_PLAN_NAME", "name cannot be blank");
        }
    }

    private void validateStatusTransition(PlanStatus current, PlanStatus next) {
        if (current == PlanStatus.COMPLETED && next != PlanStatus.COMPLETED) {
            throw BusinessException.conflict("INVALID_PLAN_STATUS_TRANSITION", "A completed plan cannot be reopened");
        }
    }
}

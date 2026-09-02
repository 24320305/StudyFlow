package com.studyflow.backend.plan;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import com.studyflow.backend.common.exception.BusinessException;
import com.studyflow.backend.user.UserAccount;
import com.studyflow.backend.user.UserAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudyPlanServiceTest {

    @Mock
    private StudyPlanRepository studyPlanRepository;

    @Mock
    private UserAccountService userAccountService;

    private StudyPlanService studyPlanService;

    @BeforeEach
    void setUp() {
        studyPlanService = new StudyPlanService(studyPlanRepository, userAccountService);
    }

    @Test
    void rejectsEndDateBeforeStartDate() {
        CreatePlanRequest request = new CreatePlanRequest(
                "Java",
                LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 9, 9),
                60);

        assertThatThrownBy(() -> studyPlanService.create(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("endDate cannot be earlier than startDate");
    }

    @Test
    void hidesPlansOwnedByAnotherUser() {
        when(studyPlanRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studyPlanService.get(1L, 99L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Study plan was not found");
    }

    @Test
    void completedPlanCannotBeReopened() {
        UserAccount user = UserAccount.register("owner@example.com", "hash", "Owner");
        StudyPlan plan = new StudyPlan(
                user,
                "Java",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30),
                60);
        plan.changeStatus(PlanStatus.COMPLETED);
        when(studyPlanRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(plan));

        UpdatePlanRequest request = new UpdatePlanRequest(null, null, null, null, PlanStatus.ACTIVE);
        assertThatThrownBy(() -> studyPlanService.update(1L, 1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("A completed plan cannot be reopened");
    }
}

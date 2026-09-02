package com.studyflow.backend.plan;

import com.studyflow.backend.common.api.ApiResponse;
import com.studyflow.backend.common.api.PageResponse;
import com.studyflow.backend.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plans")
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    public StudyPlanController(StudyPlanService studyPlanService) {
        this.studyPlanService = studyPlanService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudyPlanResponse>> create(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreatePlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(studyPlanService.create(currentUser.id(), request)));
    }

    @GetMapping
    public ApiResponse<PageResponse<StudyPlanResponse>> list(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) PlanStatus status) {
        return ApiResponse.success(studyPlanService.list(currentUser.id(), page, pageSize, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<StudyPlanResponse> get(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        return ApiResponse.success(studyPlanService.get(currentUser.id(), id));
    }

    @PatchMapping("/{id}")
    public ApiResponse<StudyPlanResponse> update(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdatePlanRequest request) {
        return ApiResponse.success(studyPlanService.update(currentUser.id(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        studyPlanService.delete(currentUser.id(), id);
        return ApiResponse.success(null);
    }
}

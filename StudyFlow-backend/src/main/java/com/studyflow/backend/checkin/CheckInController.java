package com.studyflow.backend.checkin;

import java.time.LocalDate;
import java.util.List;

import com.studyflow.backend.common.api.ApiResponse;
import com.studyflow.backend.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plans/{planId}/check-ins")
public class CheckInController {

    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @PutMapping("/{checkDate}")
    public ApiResponse<CheckInResponse> upsert(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long planId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkDate,
            @Valid @RequestBody UpsertCheckInRequest request) {
        return ApiResponse.success(checkInService.upsert(currentUser.id(), planId, checkDate, request));
    }

    @GetMapping
    public ApiResponse<List<CheckInResponse>> list(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long planId) {
        return ApiResponse.success(checkInService.list(currentUser.id(), planId));
    }
}

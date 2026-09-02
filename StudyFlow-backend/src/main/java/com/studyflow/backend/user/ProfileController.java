package com.studyflow.backend.user;

import com.studyflow.backend.common.api.ApiResponse;
import com.studyflow.backend.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ApiResponse<UserProfileResponse> get(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ApiResponse.success(profileService.get(currentUser.id()));
    }

    @PatchMapping
    public ApiResponse<UserProfileResponse> update(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.success(profileService.update(currentUser.id(), request));
    }
}

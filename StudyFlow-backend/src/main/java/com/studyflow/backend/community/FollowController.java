package com.studyflow.backend.community;

import com.studyflow.backend.common.api.ApiResponse;
import com.studyflow.backend.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class FollowController {

    private final CommunityService communityService;

    public FollowController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @PostMapping("/{id}/follow")
    public ApiResponse<FollowStateResponse> follow(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        return ApiResponse.success(communityService.follow(currentUser.id(), id));
    }

    @DeleteMapping("/{id}/follow")
    public ApiResponse<FollowStateResponse> unfollow(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        return ApiResponse.success(communityService.unfollow(currentUser.id(), id));
    }
}

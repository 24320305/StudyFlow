package com.studyflow.backend.community;

import com.studyflow.backend.common.api.ApiResponse;
import com.studyflow.backend.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommunityService communityService;

    public CommentController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        communityService.deleteComment(currentUser.id(), id);
        return ApiResponse.success(null);
    }
}

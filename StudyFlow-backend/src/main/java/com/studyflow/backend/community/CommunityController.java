package com.studyflow.backend.community;

import java.util.List;

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
@RequestMapping("/api/posts")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> publish(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreatePostRequest request) {
        PostWriteResult result = communityService.publish(currentUser.id(), request);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(ApiResponse.success(result.post()));
    }

    @GetMapping
    public ApiResponse<PageResponse<PostResponse>> list(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(communityService.listPublic(currentUser.id(), keyword, page, pageSize));
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<PostResponse>> search(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(communityService.listPublic(currentUser.id(), keyword, page, pageSize));
    }

    @GetMapping("/mine")
    public ApiResponse<PageResponse<PostResponse>> mine(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(communityService.listMine(currentUser.id(), page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<PostResponse> get(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        return ApiResponse.success(communityService.get(currentUser.id(), id));
    }

    @PatchMapping("/{id}")
    public ApiResponse<PostResponse> update(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdatePostRequest request) {
        return ApiResponse.success(communityService.update(currentUser.id(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePost(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        communityService.deletePost(currentUser.id(), id);
        return ApiResponse.success(null);
    }

    @PostMapping({"/{id}/likes", "/{id}/like"})
    public ApiResponse<LikeStateResponse> like(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        return ApiResponse.success(communityService.like(currentUser.id(), id));
    }

    @DeleteMapping({"/{id}/likes", "/{id}/like"})
    public ApiResponse<LikeStateResponse> unlike(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        return ApiResponse.success(communityService.unlike(currentUser.id(), id));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id,
            @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(communityService.addComment(currentUser.id(), id, request)));
    }

    @GetMapping("/{id}/comments")
    public ApiResponse<List<CommentResponse>> comments(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        return ApiResponse.success(communityService.listComments(currentUser.id(), id));
    }
}

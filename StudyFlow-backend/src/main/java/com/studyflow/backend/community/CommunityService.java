package com.studyflow.backend.community;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import com.studyflow.backend.checkin.CheckIn;
import com.studyflow.backend.checkin.CheckInService;
import com.studyflow.backend.common.api.PageResponse;
import com.studyflow.backend.common.exception.BusinessException;
import com.studyflow.backend.user.UserAccount;
import com.studyflow.backend.user.UserAccountService;
import com.studyflow.backend.user.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CommunityService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final FollowRepository followRepository;
    private final CheckInService checkInService;
    private final UserAccountService userAccountService;

    public CommunityService(
            PostRepository postRepository,
            CommentRepository commentRepository,
            PostLikeRepository postLikeRepository,
            FollowRepository followRepository,
            CheckInService checkInService,
            UserAccountService userAccountService) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.postLikeRepository = postLikeRepository;
        this.followRepository = followRepository;
        this.checkInService = checkInService;
        this.userAccountService = userAccountService;
    }

    @Transactional
    public PostWriteResult publish(Long userId, CreatePostRequest request) {
        UserAccount user = requireCommunityWriter(userId);
        CheckIn checkIn = checkInService.getOwnedCheckInForPublication(userId, request.checkInId());
        if (!checkIn.isCompleted()) {
            throw BusinessException.badRequest("CHECK_IN_NOT_COMPLETED", "Only a completed check-in can be shared as a post");
        }

        var existing = postRepository.findByCheckInId(checkIn.getId());
        if (existing.isPresent()) {
            return new PostWriteResult(toPostResponse(existing.get(), userId), false);
        }

        Post post = new Post(user, checkIn, request.content().trim(), request.visibility());
        Post saved = postRepository.saveAndFlush(post);
        return new PostWriteResult(toPostResponse(saved, userId), true);
    }

    public PageResponse<PostResponse> listPublic(Long userId, String keyword, int page, int pageSize) {
        PageRequest pageRequest = pageRequest(page, pageSize);
        String normalizedKeyword = trimToNull(keyword);
        Page<Post> result = normalizedKeyword == null
                ? postRepository.findAllByVisibilityAndStatusAndUser_Status(
                        PostVisibility.PUBLIC, PostStatus.VISIBLE, UserStatus.NORMAL, pageRequest)
                : postRepository.findAllByVisibilityAndStatusAndUser_StatusAndContentContainingIgnoreCase(
                        PostVisibility.PUBLIC, PostStatus.VISIBLE, UserStatus.NORMAL, normalizedKeyword, pageRequest);
        return new PageResponse<>(
                result.getContent().stream().map(post -> toPostResponse(post, userId)).toList(),
                page,
                pageSize,
                result.getTotalElements());
    }

    public PageResponse<PostResponse> listMine(Long userId, int page, int pageSize) {
        PageRequest pageRequest = pageRequest(page, pageSize);
        Page<Post> result = postRepository.findAllByUserIdAndStatusNot(userId, PostStatus.DELETED, pageRequest);
        return new PageResponse<>(
                result.getContent().stream().map(post -> toPostResponse(post, userId)).toList(),
                page,
                pageSize,
                result.getTotalElements());
    }

    public PostResponse get(Long userId, Long postId) {
        return toPostResponse(getVisiblePost(userId, postId), userId);
    }

    @Transactional
    public PostResponse update(Long userId, Long postId, UpdatePostRequest request) {
        if (request.content() == null && request.visibility() == null) {
            throw BusinessException.badRequest("EMPTY_POST_UPDATE", "Provide content or visibility to update");
        }
        requireCommunityWriter(userId);
        Post post = getOwnedPost(userId, postId);
        ensureEditable(post);
        post.update(request.content() == null ? null : request.content().trim(), request.visibility());
        return toPostResponse(postRepository.saveAndFlush(post), userId);
    }

    @Transactional
    public void deletePost(Long userId, Long postId) {
        requireCommunityWriter(userId);
        Post post = getOwnedPost(userId, postId);
        ensureEditable(post);
        post.markDeleted();
        postRepository.saveAndFlush(post);
    }

    @Transactional
    public LikeStateResponse like(Long userId, Long postId) {
        UserAccount user = requireCommunityWriter(userId);
        Post post = getPublicInteractionPostForLike(postId);
        if (!postLikeRepository.existsByUserIdAndPostId(userId, postId)) {
            postLikeRepository.saveAndFlush(new PostLike(user, post));
        }
        return new LikeStateResponse(postId, true, postLikeRepository.countByPostId(postId));
    }

    @Transactional
    public LikeStateResponse unlike(Long userId, Long postId) {
        requireCommunityWriter(userId);
        getPublicInteractionPost(postId);
        postLikeRepository.deleteByUserIdAndPostId(userId, postId);
        return new LikeStateResponse(postId, false, postLikeRepository.countByPostId(postId));
    }

    @Transactional
    public CommentResponse addComment(Long userId, Long postId, CreateCommentRequest request) {
        UserAccount user = requireCommunityWriter(userId);
        Post post = getPublicInteractionPost(postId);
        Comment comment = new Comment(user, post, request.content().trim());
        return toCommentResponse(commentRepository.saveAndFlush(comment));
    }

    public List<CommentResponse> listComments(Long userId, Long postId) {
        getVisiblePost(userId, postId);
        return commentRepository.findAllByPostIdAndStatusOrderByCreatedAtAsc(postId, CommentStatus.VISIBLE).stream()
                .map(this::toCommentResponse)
                .toList();
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        requireCommunityWriter(userId);
        Comment comment = commentRepository.findByIdAndUserId(commentId, userId)
                .orElseThrow(() -> BusinessException.notFound("COMMENT_NOT_FOUND", "Comment was not found"));
        if (comment.getStatus() == CommentStatus.DELETED) {
            throw BusinessException.notFound("COMMENT_NOT_FOUND", "Comment was not found");
        }
        comment.markDeleted();
        commentRepository.saveAndFlush(comment);
    }

    @Transactional
    public FollowStateResponse follow(Long userId, Long targetUserId) {
        UserAccount follower = requireCommunityWriterForFollow(userId);
        if (userId.equals(targetUserId)) {
            throw BusinessException.badRequest("FOLLOW_SELF", "You cannot follow yourself");
        }
        UserAccount target = userAccountService.getRequired(targetUserId);
        if (target.getStatus() != UserStatus.NORMAL) {
            throw BusinessException.notFound("USER_NOT_FOUND", "User was not found");
        }
        if (!followRepository.existsByFollowerIdAndFollowingId(userId, targetUserId)) {
            followRepository.saveAndFlush(new FollowRelation(follower, target));
        }
        return new FollowStateResponse(targetUserId, true);
    }

    @Transactional
    public FollowStateResponse unfollow(Long userId, Long targetUserId) {
        requireCommunityWriter(userId);
        followRepository.deleteByFollowerIdAndFollowingId(userId, targetUserId);
        return new FollowStateResponse(targetUserId, false);
    }

    /**
     * Entry point for the later administrator module. It deliberately does not
     * expose content status changes through a normal-user HTTP endpoint.
     */
    @Transactional
    public void changeModerationStatus(Long postId, PostStatus nextStatus) {
        if (nextStatus != PostStatus.VISIBLE && nextStatus != PostStatus.HIDDEN) {
            throw BusinessException.badRequest("INVALID_POST_STATUS", "Moderation can only set VISIBLE or HIDDEN");
        }
        Post post = findPost(postId);
        ensureEditable(post);
        post.changeModerationStatus(nextStatus);
        postRepository.saveAndFlush(post);
    }

    private Post getVisiblePost(Long currentUserId, Long postId) {
        Post post = findPost(postId);
        if (post.getUser().getId().equals(currentUserId) && post.getStatus() != PostStatus.DELETED) {
            return post;
        }
        if (isPubliclyVisible(post)) {
            return post;
        }
        throw BusinessException.notFound("POST_NOT_FOUND", "Post was not found");
    }

    private Post getPublicInteractionPost(Long postId) {
        Post post = findPost(postId);
        if (!isPubliclyVisible(post)) {
            throw BusinessException.notFound("POST_NOT_FOUND", "Post was not found");
        }
        return post;
    }

    private Post getPublicInteractionPostForLike(Long postId) {
        Post post = postRepository.findByIdForInteraction(postId)
                .orElseThrow(() -> BusinessException.notFound("POST_NOT_FOUND", "Post was not found"));
        if (!isPubliclyVisible(post)) {
            throw BusinessException.notFound("POST_NOT_FOUND", "Post was not found");
        }
        return post;
    }

    private Post getOwnedPost(Long userId, Long postId) {
        Post post = findPost(postId);
        if (!post.getUser().getId().equals(userId)) {
            throw BusinessException.notFound("POST_NOT_FOUND", "Post was not found");
        }
        return post;
    }

    private Post findPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> BusinessException.notFound("POST_NOT_FOUND", "Post was not found"));
    }

    private boolean isPubliclyVisible(Post post) {
        return post.getVisibility() == PostVisibility.PUBLIC
                && post.getStatus() == PostStatus.VISIBLE
                && post.getUser().getStatus() == UserStatus.NORMAL;
    }

    private void ensureEditable(Post post) {
        if (post.getStatus() == PostStatus.DELETED) {
            throw BusinessException.notFound("POST_NOT_FOUND", "Post was not found");
        }
    }

    private UserAccount requireCommunityWriter(Long userId) {
        return requireCommunityWriter(userAccountService.getRequired(userId));
    }

    private UserAccount requireCommunityWriterForFollow(Long userId) {
        return requireCommunityWriter(userAccountService.getRequiredForFollow(userId));
    }

    private UserAccount requireCommunityWriter(UserAccount user) {
        if (user.getStatus() != UserStatus.NORMAL) {
            throw BusinessException.forbidden("ACCOUNT_RESTRICTED", "This account cannot perform community actions");
        }
        return user;
    }

    private PageRequest pageRequest(int page, int pageSize) {
        if (page < 1 || pageSize < 1 || pageSize > 100) {
            throw BusinessException.badRequest("INVALID_PAGE", "page must be >= 1 and pageSize must be between 1 and 100");
        }
        return PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private PostResponse toPostResponse(Post post, Long currentUserId) {
        Long checkInId = post.getCheckIn() == null ? null : post.getCheckIn().getId();
        return new PostResponse(
                post.getId(),
                CommunityUserResponse.from(post.getUser()),
                checkInId,
                post.getContent(),
                post.getVisibility(),
                post.getStatus(),
                postLikeRepository.countByPostId(post.getId()),
                commentRepository.countByPostIdAndStatus(post.getId(), CommentStatus.VISIBLE),
                postLikeRepository.existsByUserIdAndPostId(currentUserId, post.getId()),
                toOffsetDateTime(post.getCreatedAt()),
                toOffsetDateTime(post.getUpdatedAt()));
    }

    private CommentResponse toCommentResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                CommunityUserResponse.from(comment.getUser()),
                comment.getContent(),
                comment.getStatus(),
                toOffsetDateTime(comment.getCreatedAt()));
    }

    private OffsetDateTime toOffsetDateTime(java.time.Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, ZoneId.of("Asia/Shanghai"));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

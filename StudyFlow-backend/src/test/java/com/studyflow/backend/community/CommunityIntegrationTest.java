package com.studyflow.backend.community;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyflow.backend.checkin.CheckInRepository;
import com.studyflow.backend.user.UserAccount;
import com.studyflow.backend.user.UserAccountRepository;
import com.studyflow.backend.user.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class CommunityIntegrationTest {

    private static final String CHECK_IN_DATE = "2026-09-10";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CheckInRepository checkInRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Test
    void publishesOnePostPerCompletedCheckInAndKeepsTheCheckInAfterPostDeletion() throws Exception {
        String token = register("Publisher");
        long planId = createPlan(token, "Community publishing plan");
        long checkInId = saveCompletedCheckIn(token, planId);

        assertThat(postRepository.findByCheckInId(checkInId)).isEmpty();

        long firstPostId = publish(token, checkInId, "Finished my Java study session", "PUBLIC", 201);
        long repeatedPostId = publish(token, checkInId, "This must not make a second post", "PUBLIC", 200);

        assertThat(repeatedPostId).isEqualTo(firstPostId);
        assertThat(postRepository.findByCheckInId(checkInId)).hasValueSatisfying(post ->
                assertThat(post.getId()).isEqualTo(firstPostId));

        mockMvc.perform(delete("/api/posts/{id}", firstPostId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"));

        assertThat(checkInRepository.existsById(checkInId)).isTrue();
        assertThat(postRepository.findById(firstPostId)).hasValueSatisfying(post ->
                assertThat(post.getStatus()).isEqualTo(PostStatus.DELETED));
    }

    @Test
    void keepsPrivatePostsOutOfOtherUsersDiscoveryAndDetailViews() throws Exception {
        String ownerToken = register("Private owner");
        long ownerPlanId = createPlan(ownerToken, "Private community plan");
        long checkInId = saveCompletedCheckIn(ownerToken, ownerPlanId);
        String privateMarker = "private-" + UUID.randomUUID();
        long postId = publish(ownerToken, checkInId, privateMarker, "PRIVATE", 201);
        String viewerToken = register("Viewer");

        mockMvc.perform(get("/api/posts/{id}", postId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(viewerToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"));

        mockMvc.perform(get("/api/posts/search")
                        .header(HttpHeaders.AUTHORIZATION, bearer(viewerToken))
                        .param("keyword", privateMarker))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void makesLikeAndFollowRequestsIdempotentAndRejectsBlankComments() throws Exception {
        String authorToken = register("Public author");
        long authorId = currentUserId(authorToken);
        long planId = createPlan(authorToken, "Public post plan");
        long checkInId = saveCompletedCheckIn(authorToken, planId);
        long postId = publish(authorToken, checkInId, "A public learning update", "PUBLIC", 201);

        String readerToken = register("Reader");
        long readerId = currentUserId(readerToken);

        mockMvc.perform(post("/api/posts/{id}/likes", postId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(readerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked").value(true))
                .andExpect(jsonPath("$.data.likeCount").value(1));

        mockMvc.perform(post("/api/posts/{id}/likes", postId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(readerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.likeCount").value(1));
        assertThat(postLikeRepository.countByPostId(postId)).isEqualTo(1);

        mockMvc.perform(post("/api/users/{id}/follow", authorId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(readerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.following").value(true));
        mockMvc.perform(post("/api/users/{id}/follow", authorId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(readerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.following").value(true));
        assertThat(followRepository.countByFollowerIdAndFollowingId(readerId, authorId)).isEqualTo(1);

        mockMvc.perform(post("/api/posts/{id}/comments", postId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(readerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        mockMvc.perform(delete("/api/posts/{id}/likes", postId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(readerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked").value(false))
                .andExpect(jsonPath("$.data.likeCount").value(0));
        mockMvc.perform(delete("/api/users/{id}/follow", authorId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(readerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.following").value(false));
    }

    @Test
    void preventsRestrictedAccountsFromPerformingCommunityWrites() throws Exception {
        String authorToken = register("Restricted target");
        long authorId = currentUserId(authorToken);
        long authorPlanId = createPlan(authorToken, "Restricted target plan");
        long authorCheckInId = saveCompletedCheckIn(authorToken, authorPlanId);
        long postId = publish(authorToken, authorCheckInId, "A post for access control", "PUBLIC", 201);

        String restrictedToken = register("Restricted reader");
        long restrictedId = currentUserId(restrictedToken);
        long restrictedPlanId = createPlan(restrictedToken, "Restricted personal plan");
        long restrictedCheckInId = saveCompletedCheckIn(restrictedToken, restrictedPlanId);

        mockMvc.perform(post("/api/posts/{id}/likes", postId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(restrictedToken)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/users/{id}/follow", authorId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(restrictedToken)))
                .andExpect(status().isOk());
        MvcResult commentResult = mockMvc.perform(post("/api/posts/{id}/comments", postId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(restrictedToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"A comment before restriction\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        long commentId = data(commentResult).path("id").asLong();

        UserAccount restrictedUser = userAccountRepository.findById(restrictedId).orElseThrow();
        restrictedUser.changeStatus(UserStatus.RESTRICTED);
        userAccountRepository.saveAndFlush(restrictedUser);

        mockMvc.perform(post("/api/posts")
                        .header(HttpHeaders.AUTHORIZATION, bearer(restrictedToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postPayload(restrictedCheckInId, "Blocked post", "PUBLIC")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_RESTRICTED"));
        mockMvc.perform(post("/api/posts/{id}/comments", postId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(restrictedToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Blocked comment\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_RESTRICTED"));
        mockMvc.perform(post("/api/posts/{id}/likes", postId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(restrictedToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_RESTRICTED"));
        mockMvc.perform(delete("/api/posts/{id}/likes", postId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(restrictedToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_RESTRICTED"));
        mockMvc.perform(post("/api/users/{id}/follow", authorId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(restrictedToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_RESTRICTED"));
        mockMvc.perform(delete("/api/users/{id}/follow", authorId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(restrictedToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_RESTRICTED"));
        mockMvc.perform(delete("/api/comments/{id}", commentId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(restrictedToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_RESTRICTED"));

        assertThat(postLikeRepository.existsByUserIdAndPostId(restrictedId, postId)).isTrue();
        assertThat(followRepository.existsByFollowerIdAndFollowingId(restrictedId, authorId)).isTrue();
    }

    private String register(String nickname) throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "community" + suffix + "@example.test",
                                "password", "password123",
                                "nickname", nickname))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();
        return data(result).path("accessToken").asText();
    }

    private long currentUserId(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        return data(result).path("id").asLong();
    }

    private long createPlan(String token, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/plans")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", name,
                                "startDate", "2026-09-01",
                                "endDate", "2026-09-30",
                                "dailyTarget", 60))))
                .andExpect(status().isCreated())
                .andReturn();
        return data(result).path("id").asLong();
    }

    private long saveCompletedCheckIn(String token, long planId) throws Exception {
        MvcResult result = mockMvc.perform(put("/api/plans/{planId}/check-ins/{checkDate}", planId, CHECK_IN_DATE)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"durationMinutes\":60,\"completed\":true,\"note\":\"Finished\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completed").value(true))
                .andReturn();
        return data(result).path("id").asLong();
    }

    private long publish(String token, long checkInId, String content, String visibility, int expectedStatus) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/posts")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postPayload(checkInId, content, visibility)))
                .andExpect(status().is(expectedStatus))
                .andExpect(jsonPath("$.code").value("OK"))
                .andReturn();
        return data(result).path("id").asLong();
    }

    private String postPayload(long checkInId, String content, String visibility) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "checkInId", checkInId,
                "content", content,
                "visibility", visibility));
    }

    private JsonNode data(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }
}

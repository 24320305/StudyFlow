package com.studyflow.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void completesUserPlanFlowAndRevokesLoggedOutToken() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String email = "api" + suffix + "@example.test";
        String password = "password123";

        register(email, password, "API User");
        String accessToken = login(email, password);
        long planId = createPlan(accessToken, "Java basics");

        mockMvc.perform(patch("/api/plans/{id}", planId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        mockMvc.perform(get("/api/plans")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .param("page", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        mockMvc.perform(post("/api/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"));

        mockMvc.perform(get("/api/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("REVOKED_ACCESS_TOKEN"));
    }

    @Test
    void hidesAnotherUsersPlan() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String ownerToken = register("owner" + suffix + "@example.test", "password123", "Owner");
        long planId = createPlan(ownerToken, "Owner plan");
        String otherToken = register("other" + suffix + "@example.test", "password123", "Other");

        mockMvc.perform(get("/api/plans/{id}", planId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PLAN_NOT_FOUND"));
    }

    @Test
    void filtersPlansByStatusAndStoresCheckInImageUrl() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String token = register("compat" + suffix + "@example.test", "password123", "Compatibility User");
        long activePlanId = createPlan(token, "Active plan");
        long pausedPlanId = createPlan(token, "Paused plan");

        mockMvc.perform(patch("/api/plans/{id}", pausedPlanId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PAUSED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAUSED"));

        mockMvc.perform(get("/api/plans")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("status", "PAUSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value(pausedPlanId))
                .andExpect(jsonPath("$.data.items[0].status").value("PAUSED"));

        mockMvc.perform(put("/api/plans/{planId}/check-ins/{checkDate}", activePlanId, "2026-09-10")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"durationMinutes\":60,\"completed\":true,\"note\":\"Image attached\",\"imageUrl\":\"https://example.test/check-in.png\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.imageUrl").value("https://example.test/check-in.png"));

        mockMvc.perform(get("/api/plans/{planId}/check-ins", activePlanId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].imageUrl").value("https://example.test/check-in.png"));
    }

    @Test
    void exposesHealthProbeWithoutLogin() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    private String register(String email, String password, String nickname) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", password,
                                "nickname", nickname))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.requestId").isNotEmpty())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();
        return data(result).path("accessToken").asText();
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();
        return data(result).path("accessToken").asText();
    }

    private long createPlan(String accessToken, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/plans")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","startDate":"2026-09-01","endDate":"2026-09-30","dailyTarget":60}
                                """.formatted(name)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn();
        return data(result).path("id").asLong();
    }

    private JsonNode data(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }
}

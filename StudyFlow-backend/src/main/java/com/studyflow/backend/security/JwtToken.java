package com.studyflow.backend.security;

import java.time.Instant;

import com.studyflow.backend.user.UserRole;

public record JwtToken(Long userId, UserRole role, String jti, Instant expiresAt) {
}

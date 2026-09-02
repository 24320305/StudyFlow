package com.studyflow.backend.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import com.studyflow.backend.user.UserAccount;
import com.studyflow.backend.user.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(properties.secret());
        } catch (IllegalArgumentException exception) {
            keyBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret must contain at least 32 bytes");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public IssuedToken issue(UserAccount user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.accessTokenTtl());
        String jti = UUID.randomUUID().toString();
        String accessToken = Jwts.builder()
                .issuer(properties.issuer())
                .subject(user.getId().toString())
                .id(jti)
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
        return new IssuedToken(accessToken, jti, expiresAt);
    }

    public JwtToken parse(String accessToken) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(properties.issuer())
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();
        Long userId = Long.valueOf(claims.getSubject());
        UserRole role = UserRole.valueOf(claims.get("role", String.class));
        return new JwtToken(userId, role, claims.getId(), claims.getExpiration().toInstant());
    }

    public record IssuedToken(String accessToken, String jti, Instant expiresAt) {
    }
}

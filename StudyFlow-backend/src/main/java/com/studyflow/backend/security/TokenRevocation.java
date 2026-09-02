package com.studyflow.backend.security;

import java.time.Instant;

import com.studyflow.backend.user.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "token_revocation")
public class TokenRevocation {

    @Id
    @Column(nullable = false, length = 64)
    private String jti;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected TokenRevocation() {
    }

    public TokenRevocation(String jti, UserAccount user, Instant expiresAt) {
        this.jti = jti;
        this.user = user;
        this.expiresAt = expiresAt;
    }
}

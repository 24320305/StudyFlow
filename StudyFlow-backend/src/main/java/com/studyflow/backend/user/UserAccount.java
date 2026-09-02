package com.studyflow.backend.user;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "sys_user")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 80)
    private String nickname;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserAccount() {
    }

    private UserAccount(String email, String passwordHash, String nickname, UserRole role, UserStatus status) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.role = role;
        this.status = status;
    }

    public static UserAccount register(String email, String passwordHash, String nickname) {
        return new UserAccount(email, passwordHash, nickname, UserRole.USER, UserStatus.NORMAL);
    }

    public void updateProfile(String nickname, String avatarUrl) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (avatarUrl != null) {
            this.avatarUrl = avatarUrl;
        }
    }

    public void changeStatus(UserStatus status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getNickname() {
        return nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

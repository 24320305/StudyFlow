package com.studyflow.backend.community;

import java.time.Instant;

import com.studyflow.backend.checkin.CheckIn;
import com.studyflow.backend.user.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "post", uniqueConstraints = @UniqueConstraint(name = "uk_post_checkin", columnNames = "checkin_id"))
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkin_id")
    private CheckIn checkIn;

    @Column(nullable = false, length = 500)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostVisibility visibility;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Post() {
    }

    public Post(UserAccount user, CheckIn checkIn, String content, PostVisibility visibility) {
        this.user = user;
        this.checkIn = checkIn;
        this.content = content;
        this.visibility = visibility;
        this.status = PostStatus.VISIBLE;
    }

    public void update(String content, PostVisibility visibility) {
        if (content != null) {
            this.content = content;
        }
        if (visibility != null) {
            this.visibility = visibility;
        }
    }

    public void markDeleted() {
        this.status = PostStatus.DELETED;
    }

    public void changeModerationStatus(PostStatus status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public UserAccount getUser() {
        return user;
    }

    public CheckIn getCheckIn() {
        return checkIn;
    }

    public String getContent() {
        return content;
    }

    public PostVisibility getVisibility() {
        return visibility;
    }

    public PostStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

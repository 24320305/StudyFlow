package com.studyflow.backend.community;

import java.time.Instant;

import com.studyflow.backend.user.UserAccount;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "post_like", uniqueConstraints = @UniqueConstraint(
        name = "uk_post_like_user_post",
        columnNames = {"user_id", "post_id"}))
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @CreationTimestamp
    @jakarta.persistence.Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PostLike() {
    }

    public PostLike(UserAccount user, Post post) {
        this.user = user;
        this.post = post;
    }

    public Long getId() {
        return id;
    }
}

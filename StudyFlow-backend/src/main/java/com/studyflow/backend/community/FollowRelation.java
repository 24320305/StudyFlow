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
@Table(name = "follow", uniqueConstraints = @UniqueConstraint(
        name = "uk_follow_follower_following",
        columnNames = {"follower_id", "following_id"}))
public class FollowRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "follower_id", nullable = false)
    private UserAccount follower;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "following_id", nullable = false)
    private UserAccount following;

    @CreationTimestamp
    @jakarta.persistence.Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected FollowRelation() {
    }

    public FollowRelation(UserAccount follower, UserAccount following) {
        this.follower = follower;
        this.following = following;
    }

    public Long getId() {
        return id;
    }

    public UserAccount getFollower() {
        return follower;
    }

    public UserAccount getFollowing() {
        return following;
    }
}

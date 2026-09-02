package com.studyflow.backend.checkin;

import java.time.Instant;
import java.time.LocalDate;

import com.studyflow.backend.plan.StudyPlan;
import com.studyflow.backend.user.UserAccount;
import jakarta.persistence.Column;
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
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "check_in", uniqueConstraints = @UniqueConstraint(
        name = "uk_check_in_user_plan_date",
        columnNames = {"user_id", "plan_id", "check_date"}))
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private StudyPlan plan;

    @Column(name = "check_date", nullable = false)
    private LocalDate checkDate;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    private boolean completed;

    @Column(length = 500)
    private String note;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CheckIn() {
    }

    public CheckIn(
            UserAccount user,
            StudyPlan plan,
            LocalDate checkDate,
            Integer durationMinutes,
            boolean completed,
            String note) {
        this(user, plan, checkDate, durationMinutes, completed, note, null);
    }

    public CheckIn(
            UserAccount user,
            StudyPlan plan,
            LocalDate checkDate,
            Integer durationMinutes,
            boolean completed,
            String note,
            String imageUrl) {
        this.user = user;
        this.plan = plan;
        this.checkDate = checkDate;
        this.durationMinutes = durationMinutes;
        this.completed = completed;
        this.note = note;
        this.imageUrl = imageUrl;
    }

    public void update(Integer durationMinutes, boolean completed, String note) {
        update(durationMinutes, completed, note, this.imageUrl);
    }

    public void update(Integer durationMinutes, boolean completed, String note, String imageUrl) {
        this.durationMinutes = durationMinutes;
        this.completed = completed;
        this.note = note;
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public UserAccount getUser() {
        return user;
    }

    public StudyPlan getPlan() {
        return plan;
    }

    public LocalDate getCheckDate() {
        return checkDate;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getNote() {
        return note;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

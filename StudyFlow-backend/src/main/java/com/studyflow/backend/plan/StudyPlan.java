package com.studyflow.backend.plan;

import java.time.Instant;
import java.time.LocalDate;

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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "study_plan")
public class StudyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "daily_target", nullable = false)
    private Integer dailyTarget;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected StudyPlan() {
    }

    public StudyPlan(UserAccount user, String name, LocalDate startDate, LocalDate endDate, Integer dailyTarget) {
        this.user = user;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dailyTarget = dailyTarget;
        this.status = PlanStatus.ACTIVE;
    }

    public void update(String name, LocalDate startDate, LocalDate endDate, Integer dailyTarget) {
        if (name != null) {
            this.name = name;
        }
        if (startDate != null) {
            this.startDate = startDate;
        }
        if (endDate != null) {
            this.endDate = endDate;
        }
        if (dailyTarget != null) {
            this.dailyTarget = dailyTarget;
        }
    }

    public void changeStatus(PlanStatus status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public UserAccount getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Integer getDailyTarget() {
        return dailyTarget;
    }

    public PlanStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

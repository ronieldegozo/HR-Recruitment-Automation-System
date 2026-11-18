package org.hr.recruitmen.automation.organization.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "recruiter_progress_reports", schema = "recruitflow")
public class RecruiterProgressEntity {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "assignment_id", nullable = false, updatable = false)
    private UUID assignmentId;

    @Column(name = "recruiter_user_id", nullable = false, updatable = false)
    private UUID recruiterUserId;

    @Column(name = "period_start", nullable = false, updatable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "candidates_reviewed", nullable = false)
    private int candidatesReviewed;

    @Column(name = "candidates_contacted", nullable = false)
    private int candidatesContacted;

    @Column(name = "interviews_scheduled", nullable = false)
    private int interviewsScheduled;

    @Column(length = 2000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RecruiterProgressEntity() { }

    public RecruiterProgressEntity(UUID assignmentId, UUID recruiterUserId, LocalDate periodStart,
            LocalDate periodEnd, int candidatesReviewed, int candidatesContacted,
            int interviewsScheduled, String notes) {
        this.id = UUID.randomUUID();
        this.assignmentId = assignmentId;
        this.recruiterUserId = recruiterUserId;
        this.periodStart = periodStart;
        this.createdAt = Instant.now();
        update(periodEnd, candidatesReviewed, candidatesContacted, interviewsScheduled, notes);
    }

    public void update(LocalDate periodEnd, int candidatesReviewed, int candidatesContacted,
            int interviewsScheduled, String notes) {
        this.periodEnd = periodEnd;
        this.candidatesReviewed = candidatesReviewed;
        this.candidatesContacted = candidatesContacted;
        this.interviewsScheduled = interviewsScheduled;
        this.notes = notes;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getAssignmentId() { return assignmentId; }
    public UUID getRecruiterUserId() { return recruiterUserId; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public int getCandidatesReviewed() { return candidatesReviewed; }
    public int getCandidatesContacted() { return candidatesContacted; }
    public int getInterviewsScheduled() { return interviewsScheduled; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

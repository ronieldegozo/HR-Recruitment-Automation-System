package org.hr.recruitmen.automation.organization.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "team_lead_recruiter_assignments", schema = "recruitflow")
public class RecruiterAssignmentEntity {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "team_lead_assignment_id", nullable = false, updatable = false)
    private UUID teamLeadAssignmentId;

    @Column(name = "recruiter_user_id", nullable = false, updatable = false)
    private UUID recruiterUserId;

    @Column(name = "assigned_by_user_id", nullable = false, updatable = false)
    private UUID assignedByUserId;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    @Column(name = "unassigned_at")
    private Instant unassignedAt;

    protected RecruiterAssignmentEntity() { }

    public RecruiterAssignmentEntity(UUID teamLeadAssignmentId, UUID recruiterUserId, UUID assignedByUserId) {
        this.id = UUID.randomUUID();
        this.teamLeadAssignmentId = teamLeadAssignmentId;
        this.recruiterUserId = recruiterUserId;
        this.assignedByUserId = assignedByUserId;
        this.assignedAt = Instant.now();
    }

    public void unassign() { this.unassignedAt = Instant.now(); }
    public UUID getId() { return id; }
    public UUID getTeamLeadAssignmentId() { return teamLeadAssignmentId; }
    public UUID getRecruiterUserId() { return recruiterUserId; }
    public UUID getAssignedByUserId() { return assignedByUserId; }
    public Instant getAssignedAt() { return assignedAt; }
    public Instant getUnassignedAt() { return unassignedAt; }
}

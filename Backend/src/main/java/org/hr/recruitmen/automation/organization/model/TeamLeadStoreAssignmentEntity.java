package org.hr.recruitmen.automation.organization.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "store_team_lead_assignments", schema = "recruitflow")
public class TeamLeadStoreAssignmentEntity {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "store_id", nullable = false, updatable = false)
    private UUID storeId;

    @Column(name = "team_lead_user_id", nullable = false, updatable = false)
    private UUID teamLeadUserId;

    @Column(name = "assigned_by_user_id", nullable = false, updatable = false)
    private UUID assignedByUserId;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    @Column(name = "unassigned_at")
    private Instant unassignedAt;

    protected TeamLeadStoreAssignmentEntity() { }

    public TeamLeadStoreAssignmentEntity(UUID storeId, UUID teamLeadUserId, UUID assignedByUserId) {
        this.id = UUID.randomUUID();
        this.storeId = storeId;
        this.teamLeadUserId = teamLeadUserId;
        this.assignedByUserId = assignedByUserId;
        this.assignedAt = Instant.now();
    }

    public void unassign() { this.unassignedAt = Instant.now(); }
    public UUID getId() { return id; }
    public UUID getStoreId() { return storeId; }
    public UUID getTeamLeadUserId() { return teamLeadUserId; }
    public UUID getAssignedByUserId() { return assignedByUserId; }
    public Instant getAssignedAt() { return assignedAt; }
    public Instant getUnassignedAt() { return unassignedAt; }
}

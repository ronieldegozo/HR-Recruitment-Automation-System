package org.hr.recruitmen.automation.organization.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hr.recruitmen.automation.organization.model.RecruiterAssignmentEntity;
import org.hr.recruitmen.automation.organization.model.TeamLeadStoreAssignmentEntity;

import io.quarkus.hibernate.orm.PersistenceUnit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

@ApplicationScoped
public class AssignmentRepository {
    @Inject OrganizationEntityManager persistence;

    public Optional<TeamLeadStoreAssignmentEntity> findActiveTeamLead(UUID storeId, UUID teamLeadUserId) {
        return persistence.get().createQuery(
                "select a from TeamLeadStoreAssignmentEntity a where a.storeId = :storeId "
                        + "and a.teamLeadUserId = :userId and a.unassignedAt is null",
                TeamLeadStoreAssignmentEntity.class)
                .setParameter("storeId", storeId)
                .setParameter("userId", teamLeadUserId)
                .getResultStream().findFirst();
    }

    public Optional<TeamLeadStoreAssignmentEntity> findActiveTeamLeadForUpdate(UUID storeId, UUID teamLeadUserId) {
        return persistence.get().createQuery(
                "select a from TeamLeadStoreAssignmentEntity a where a.storeId = :storeId "
                        + "and a.teamLeadUserId = :userId and a.unassignedAt is null",
                TeamLeadStoreAssignmentEntity.class)
                .setParameter("storeId", storeId)
                .setParameter("userId", teamLeadUserId)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultStream().findFirst();
    }
    public List<TeamLeadStoreAssignmentEntity> listActiveTeamLeads(UUID storeId) {
        return persistence.get().createQuery(
                "select a from TeamLeadStoreAssignmentEntity a where a.storeId = :storeId "
                        + "and a.unassignedAt is null order by a.assignedAt",
                TeamLeadStoreAssignmentEntity.class)
                .setParameter("storeId", storeId)
                .getResultList();
    }

    public List<TeamLeadStoreAssignmentEntity> listActiveStoresForTeamLead(UUID teamLeadUserId) {
        return persistence.get().createQuery(
                "select a from TeamLeadStoreAssignmentEntity a where a.teamLeadUserId = :userId "
                        + "and a.unassignedAt is null order by a.assignedAt",
                TeamLeadStoreAssignmentEntity.class)
                .setParameter("userId", teamLeadUserId)
                .getResultList();
    }

    public void persist(TeamLeadStoreAssignmentEntity assignment) { persistence.get().persist(assignment); }

    public Optional<RecruiterAssignmentEntity> findActiveRecruiter(UUID recruiterUserId) {
        return persistence.get().createQuery(
                "select a from RecruiterAssignmentEntity a where a.recruiterUserId = :userId "
                        + "and a.unassignedAt is null",
                RecruiterAssignmentEntity.class)
                .setParameter("userId", recruiterUserId)
                .getResultStream().findFirst();
    }

    public Optional<RecruiterAssignmentEntity> findActiveRecruiterForUpdate(UUID recruiterUserId) {
        return persistence.get().createQuery(
                "select a from RecruiterAssignmentEntity a where a.recruiterUserId = :userId "
                        + "and a.unassignedAt is null",
                RecruiterAssignmentEntity.class)
                .setParameter("userId", recruiterUserId)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultStream().findFirst();
    }
    public Optional<RecruiterAssignmentEntity> findActiveRecruiter(UUID teamLeadAssignmentId, UUID recruiterUserId) {
        return persistence.get().createQuery(
                "select a from RecruiterAssignmentEntity a where a.teamLeadAssignmentId = :leadAssignmentId "
                        + "and a.recruiterUserId = :userId and a.unassignedAt is null",
                RecruiterAssignmentEntity.class)
                .setParameter("leadAssignmentId", teamLeadAssignmentId)
                .setParameter("userId", recruiterUserId)
                .getResultStream().findFirst();
    }

    public List<RecruiterAssignmentEntity> listActiveRecruiters(UUID teamLeadAssignmentId) {
        return persistence.get().createQuery(
                "select a from RecruiterAssignmentEntity a where a.teamLeadAssignmentId = :leadAssignmentId "
                        + "and a.unassignedAt is null order by a.assignedAt",
                RecruiterAssignmentEntity.class)
                .setParameter("leadAssignmentId", teamLeadAssignmentId)
                .getResultList();
    }

    public List<RecruiterAssignmentEntity> listActiveRecruitersForTeamLead(UUID teamLeadUserId) {
        return persistence.get().createQuery(
                "select r from RecruiterAssignmentEntity r, TeamLeadStoreAssignmentEntity t "
                        + "where r.teamLeadAssignmentId = t.id and t.teamLeadUserId = :userId "
                        + "and t.unassignedAt is null and r.unassignedAt is null order by r.assignedAt",
                RecruiterAssignmentEntity.class)
                .setParameter("userId", teamLeadUserId)
                .getResultList();
    }

    public boolean isRecruiterAssignedToTeamLead(UUID teamLeadUserId, UUID recruiterUserId) {
        return persistence.get().createQuery(
                "select count(r.id) from RecruiterAssignmentEntity r, TeamLeadStoreAssignmentEntity t "
                        + "where r.teamLeadAssignmentId = t.id and t.teamLeadUserId = :leadId "
                        + "and r.recruiterUserId = :recruiterId and t.unassignedAt is null "
                        + "and r.unassignedAt is null", Long.class)
                .setParameter("leadId", teamLeadUserId)
                .setParameter("recruiterId", recruiterUserId)
                .getSingleResult() > 0;
    }
    public long countActiveRecruiters(UUID teamLeadAssignmentId) {
        return persistence.get().createQuery(
                "select count(r.id) from RecruiterAssignmentEntity r where r.teamLeadAssignmentId = :id "
                        + "and r.unassignedAt is null", Long.class)
                .setParameter("id", teamLeadAssignmentId)
                .getSingleResult();
    }

    public List<UUID> listActiveTeamLeadIds() {
        return persistence.get().createQuery(
                "select a.teamLeadUserId from TeamLeadStoreAssignmentEntity a where a.unassignedAt is null",
                UUID.class).getResultList();
    }

    public List<UUID> listActiveRecruiterIds() {
        return persistence.get().createQuery(
                "select a.recruiterUserId from RecruiterAssignmentEntity a where a.unassignedAt is null",
                UUID.class).getResultList();
    }

    public void persist(RecruiterAssignmentEntity assignment) { persistence.get().persist(assignment); }
    public boolean hasActiveOrganizationLinks(UUID userId) {
        long managerStores = persistence.get().createQuery(
                "select count(s.id) from StoreEntity s where s.managerUserId = :userId and s.status = 'ACTIVE'", Long.class)
                .setParameter("userId", userId).getSingleResult();
        long teamLeadLinks = persistence.get().createQuery(
                "select count(a.id) from TeamLeadStoreAssignmentEntity a where a.teamLeadUserId = :userId and a.unassignedAt is null", Long.class)
                .setParameter("userId", userId).getSingleResult();
        long recruiterLinks = persistence.get().createQuery(
                "select count(a.id) from RecruiterAssignmentEntity a where a.recruiterUserId = :userId and a.unassignedAt is null", Long.class)
                .setParameter("userId", userId).getSingleResult();
        return managerStores > 0 || teamLeadLinks > 0 || recruiterLinks > 0;
    }
    public void flush() { persistence.get().flush(); }
}








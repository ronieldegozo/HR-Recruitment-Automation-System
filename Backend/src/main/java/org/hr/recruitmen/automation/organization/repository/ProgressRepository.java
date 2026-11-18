package org.hr.recruitmen.automation.organization.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hr.recruitmen.automation.organization.model.RecruiterProgressEntity;

import io.quarkus.hibernate.orm.PersistenceUnit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class ProgressRepository {
    @Inject OrganizationEntityManager persistence;

    public Optional<RecruiterProgressEntity> find(UUID assignmentId, LocalDate periodStart) {
        return persistence.get().createQuery(
                "select p from RecruiterProgressEntity p where p.assignmentId = :assignmentId "
                        + "and p.periodStart = :periodStart",
                RecruiterProgressEntity.class)
                .setParameter("assignmentId", assignmentId)
                .setParameter("periodStart", periodStart)
                .getResultStream().findFirst();
    }

    public List<RecruiterProgressEntity> listForRecruiter(UUID recruiterUserId) {
        return persistence.get().createQuery(
                "select p from RecruiterProgressEntity p where p.recruiterUserId = :userId "
                        + "order by p.periodStart desc, p.createdAt desc",
                RecruiterProgressEntity.class)
                .setParameter("userId", recruiterUserId)
                .setMaxResults(100)
                .getResultList();
    }

    public List<RecruiterProgressEntity> listForTeamLead(UUID teamLeadUserId, UUID recruiterUserId) {
        return persistence.get().createQuery(
                "select p from RecruiterProgressEntity p, RecruiterAssignmentEntity r, "
                        + "TeamLeadStoreAssignmentEntity t where p.assignmentId = r.id "
                        + "and r.teamLeadAssignmentId = t.id and t.teamLeadUserId = :leadId "
                        + "and p.recruiterUserId = :recruiterId and r.unassignedAt is null "
                        + "and t.unassignedAt is null order by p.periodStart desc, p.createdAt desc",
                RecruiterProgressEntity.class)
                .setParameter("leadId", teamLeadUserId)
                .setParameter("recruiterId", recruiterUserId)
                .setMaxResults(100)
                .getResultList();
    }

    public void persist(RecruiterProgressEntity report) { persistence.get().persist(report); }
    public void flush() { persistence.get().flush(); }
}




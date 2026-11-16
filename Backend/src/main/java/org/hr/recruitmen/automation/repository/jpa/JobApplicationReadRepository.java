package org.hr.recruitmen.automation.repository.jpa;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import org.hr.recruitmen.automation.model.JobApplicationEntity;

/** Narrow JPA repository for privacy-safe, read-only diagnostics. */
@ApplicationScoped
@Transactional(Transactional.TxType.SUPPORTS)
public class JobApplicationReadRepository {

    @PersistenceContext
    EntityManager entityManager;

    public long countApplications() {
        return entityManager.createQuery(
                "select count(application.id) from JobApplicationEntity application", Long.class)
                .getSingleResult();
    }
}

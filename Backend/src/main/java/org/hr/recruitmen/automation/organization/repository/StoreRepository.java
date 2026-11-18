package org.hr.recruitmen.automation.organization.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hr.recruitmen.automation.organization.model.StoreEntity;

import io.quarkus.hibernate.orm.PersistenceUnit;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class StoreRepository {
    @Inject OrganizationEntityManager persistence;

    public void persist(StoreEntity store) { persistence.get().persist(store); }

    public Optional<StoreEntity> findActiveOwned(UUID storeId, UUID managerUserId) {
        return persistence.get().createQuery(
                "select s from StoreEntity s where s.id = :id and s.managerUserId = :managerId and s.status = 'ACTIVE'",
                StoreEntity.class)
                .setParameter("id", storeId)
                .setParameter("managerId", managerUserId)
                .getResultStream().findFirst();
    }

    public List<StoreEntity> listActiveOwned(UUID managerUserId) {
        return persistence.get().createQuery(
                "select s from StoreEntity s where s.managerUserId = :managerId and s.status = 'ACTIVE' order by s.name",
                StoreEntity.class)
                .setParameter("managerId", managerUserId)
                .getResultList();
    }

    public boolean hasStoreCode(String storeCode) {
        return persistence.get().createQuery(
                "select count(s.id) from StoreEntity s where s.storeCode = :code", Long.class)
                .setParameter("code", storeCode)
                .getSingleResult() > 0;
    }

    public List<StoreEntity> listActiveForTeamLead(UUID teamLeadUserId) {
        return persistence.get().createQuery(
                "select s from StoreEntity s, TeamLeadStoreAssignmentEntity a "
                        + "where a.storeId = s.id and a.teamLeadUserId = :userId "
                        + "and a.unassignedAt is null and s.status = 'ACTIVE' order by s.name",
                StoreEntity.class)
                .setParameter("userId", teamLeadUserId)
                .getResultList();
    }
    public void flush() { persistence.get().flush(); }
}




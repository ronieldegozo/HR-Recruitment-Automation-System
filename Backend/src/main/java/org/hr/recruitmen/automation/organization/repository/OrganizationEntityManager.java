package org.hr.recruitmen.automation.organization.repository;

import io.quarkus.hibernate.orm.PersistenceUnit;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hr.recruitmen.automation.security.AuthApiException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class OrganizationEntityManager {
    @Inject
    @PersistenceUnit("organization")
    Instance<EntityManager> entityManager;

    @ConfigProperty(name = "recruitflow.organization.db.enabled", defaultValue = "false")
    boolean enabled;

    public boolean isEnabled() { return enabled; }

    public EntityManager get() {
        if (!enabled || !entityManager.isResolvable()) {
            throw new AuthApiException(503, "Store management database is not configured.");
        }
        return entityManager.get();
    }
}


package org.hr.recruitmen.automation.organization.service;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hr.recruitmen.automation.organization.repository.OrganizationEntityManager;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class OrganizationDatabaseReadinessCheck implements HealthCheck {
    @Inject OrganizationEntityManager persistence;

    @ConfigProperty(name = "recruitflow.organization.db.enabled", defaultValue = "false")
    boolean enabled;

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public HealthCheckResponse call() {
        if (!enabled) {
            return HealthCheckResponse.named("organization-database").up().withData("enabled", false).build();
        }
        try {
            persistence.get().createNativeQuery("SELECT 1").getSingleResult();
            return HealthCheckResponse.named("organization-database").up().build();
        } catch (RuntimeException e) {
            return HealthCheckResponse.named("organization-database").down().withData("dependency", "Organization database").build();
        }
    }
}

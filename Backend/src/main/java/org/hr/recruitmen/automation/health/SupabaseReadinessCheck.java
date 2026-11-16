package org.hr.recruitmen.automation.health;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import org.hr.recruitmen.automation.service.JobApplicationReadService;

/** Checks database connectivity without returning applicant data. */
@Readiness
@ApplicationScoped
public class SupabaseReadinessCheck implements HealthCheck {

    private static final Logger LOG = Logger.getLogger(SupabaseReadinessCheck.class.getName());

    @Inject
    JobApplicationReadService readService;

    private final AtomicBoolean readinessLogged = new AtomicBoolean();

    @Override
    public HealthCheckResponse call() {
        try {
            readService.countApplications();
            if (readinessLogged.compareAndSet(false, true)) {
                LOG.info("Supabase Postgres readiness check passed");
            }
            return HealthCheckResponse.up("supabase-postgres-readiness");
        } catch (RuntimeException exception) {
            return HealthCheckResponse.named("supabase-postgres-readiness")
                    .down()
                    .withData("dependency", "Supabase Postgres")
                    .build();
        }
    }
}

package org.hr.recruitmen.automation.organization.service;

import java.util.UUID;

import org.hr.recruitmen.automation.organization.repository.AssignmentRepository;
import org.hr.recruitmen.automation.organization.repository.OrganizationEntityManager;
import org.hr.recruitmen.automation.security.AuthApiException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OrganizationAccountGuardService {
    @Inject OrganizationEntityManager persistence;
    @Inject AssignmentRepository assignmentRepository;

    public void requireUnassignedBeforeRoleChangeOrDelete(UUID userId) {
        if (persistence.isEnabled() && assignmentRepository.hasActiveOrganizationLinks(userId)) {
            throw new AuthApiException(409,
                    "Unassign this account from active stores and teams before changing its role or deleting it.");
        }
    }
}

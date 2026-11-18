package org.hr.recruitmen.automation.organization.dto;

import java.time.Instant;
import java.util.UUID;

import org.hr.recruitmen.automation.security.AppRole;

public record AssignmentResponseDTO(UUID userId, String email, AppRole role, Instant assignedAt) { }

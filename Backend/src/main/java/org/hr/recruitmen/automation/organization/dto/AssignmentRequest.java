package org.hr.recruitmen.automation.organization.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AssignmentRequest(@NotNull UUID userId) { }

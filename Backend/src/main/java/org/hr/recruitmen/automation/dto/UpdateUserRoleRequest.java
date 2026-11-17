package org.hr.recruitmen.automation.dto;

import org.hr.recruitmen.automation.security.AppRole;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(@NotNull AppRole role) {
}
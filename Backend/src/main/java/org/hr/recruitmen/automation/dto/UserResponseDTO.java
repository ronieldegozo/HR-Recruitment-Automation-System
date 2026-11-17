package org.hr.recruitmen.automation.dto;

import java.util.UUID;
import org.hr.recruitmen.automation.security.AppRole;

public record UserResponseDTO(UUID userId, String email, AppRole role) {
}
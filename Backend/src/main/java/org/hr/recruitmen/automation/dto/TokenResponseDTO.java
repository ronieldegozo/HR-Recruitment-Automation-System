package org.hr.recruitmen.automation.dto;

import java.util.UUID;
import org.hr.recruitmen.automation.security.AppRole;

public record TokenResponseDTO(
        String accessToken,
        String tokenType,
        long expiresIn,
        String refreshToken,
        UUID userId,
        String email,
        AppRole role) {
}
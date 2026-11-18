package org.hr.recruitmen.automation.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateStoreRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 300) String location) { }

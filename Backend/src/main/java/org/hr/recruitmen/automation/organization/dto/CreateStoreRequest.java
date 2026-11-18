package org.hr.recruitmen.automation.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateStoreRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9][A-Za-z0-9._-]{1,31}") String storeCode,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 300) String location) { }

package org.hr.recruitmen.automation.organization.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecruiterProgressRequest(
        @NotNull java.time.LocalDate periodEnd,
        @Min(0) int candidatesReviewed,
        @Min(0) int candidatesContacted,
        @Min(0) int interviewsScheduled,
        @Size(max = 2000) String notes) { }

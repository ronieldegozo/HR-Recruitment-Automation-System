package org.hr.recruitmen.automation.organization.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.hr.recruitmen.automation.organization.model.RecruiterProgressEntity;

public record RecruiterProgressResponseDTO(UUID reportId, UUID recruiterUserId,
        LocalDate periodStart, LocalDate periodEnd, int candidatesReviewed,
        int candidatesContacted, int interviewsScheduled, String notes,
        Instant createdAt, Instant updatedAt) {
    public static RecruiterProgressResponseDTO from(RecruiterProgressEntity report) {
        return new RecruiterProgressResponseDTO(report.getId(), report.getRecruiterUserId(),
                report.getPeriodStart(), report.getPeriodEnd(), report.getCandidatesReviewed(),
                report.getCandidatesContacted(), report.getInterviewsScheduled(), report.getNotes(),
                report.getCreatedAt(), report.getUpdatedAt());
    }
}

package org.hr.recruitmen.automation.organization.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.hr.recruitmen.automation.organization.dto.RecruiterProgressRequest;
import org.hr.recruitmen.automation.organization.dto.RecruiterProgressResponseDTO;
import org.hr.recruitmen.automation.organization.model.RecruiterAssignmentEntity;
import org.hr.recruitmen.automation.organization.model.RecruiterProgressEntity;
import org.hr.recruitmen.automation.organization.repository.AssignmentRepository;
import org.hr.recruitmen.automation.organization.repository.ProgressRepository;
import org.hr.recruitmen.automation.security.AuthApiException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class RecruiterProgressService {
    @Inject AssignmentRepository assignmentRepository;
    @Inject ProgressRepository progressRepository;

    public List<RecruiterProgressResponseDTO> myProgress(UUID recruiterId) {
        return progressRepository.listForRecruiter(recruiterId).stream()
                .map(RecruiterProgressResponseDTO::from).toList();
    }

    @Transactional
    public RecruiterProgressResponseDTO upsert(UUID recruiterId, LocalDate periodStart, RecruiterProgressRequest request) {
        if (request.periodEnd().isBefore(periodStart) || request.periodEnd().isAfter(LocalDate.now())) {
            throw new AuthApiException(400, "The progress period must be valid and cannot end in the future.");
        }
        RecruiterAssignmentEntity assignment = assignmentRepository.findActiveRecruiterForUpdate(recruiterId)
                .orElseThrow(() -> new AuthApiException(409, "You must have an active Team Lead assignment to submit progress."));
        RecruiterProgressEntity report = progressRepository.find(assignment.getId(), periodStart).orElse(null);
        if (report == null) {
            report = new RecruiterProgressEntity(assignment.getId(), recruiterId, periodStart,
                    request.periodEnd(), request.candidatesReviewed(), request.candidatesContacted(),
                    request.interviewsScheduled(), clean(request.notes()));
            try {
                progressRepository.persist(report);
                progressRepository.flush();
            } catch (PersistenceException e) {
                throw new AuthApiException(409, "A report for this period was submitted at the same time. Refresh and retry.");
            }
        } else {
            report.update(request.periodEnd(), request.candidatesReviewed(), request.candidatesContacted(),
                    request.interviewsScheduled(), clean(request.notes()));
        }
        return RecruiterProgressResponseDTO.from(report);
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}



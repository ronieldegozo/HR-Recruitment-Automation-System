package org.hr.recruitmen.automation.organization.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

import org.hr.recruitmen.automation.dto.UserResponseDTO;
import org.hr.recruitmen.automation.organization.dto.AssignmentResponseDTO;
import org.hr.recruitmen.automation.organization.dto.RecruiterProgressResponseDTO;
import org.hr.recruitmen.automation.organization.dto.StoreResponseDTO;
import org.hr.recruitmen.automation.organization.model.RecruiterAssignmentEntity;
import org.hr.recruitmen.automation.organization.repository.AssignmentRepository;
import org.hr.recruitmen.automation.organization.repository.ProgressRepository;
import org.hr.recruitmen.automation.organization.repository.StoreRepository;
import org.hr.recruitmen.automation.security.AppRole;
import org.hr.recruitmen.automation.security.AuthApiException;
import org.hr.recruitmen.automation.service.SupabaseAuthService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TeamLeadMonitoringService {
    @Inject AssignmentRepository assignmentRepository;
    @Inject StoreRepository storeRepository;
    @Inject ProgressRepository progressRepository;
    @Inject SupabaseAuthService authService;

    public List<StoreResponseDTO> listStores(UUID teamLeadId) {
        return storeRepository.listActiveForTeamLead(teamLeadId).stream().map(StoreResponseDTO::from).toList();
    }

    public List<AssignmentResponseDTO> listRecruiters(UUID teamLeadId) {
        Map<UUID, UserResponseDTO> users = new HashMap<>();
        authService.listUsersByRole(AppRole.RECRUITER).forEach(user -> users.put(user.userId(), user));
        return assignmentRepository.listActiveRecruitersForTeamLead(teamLeadId).stream()
                .map(assignment -> toAssignment(assignment, users)).toList();
    }

    public List<RecruiterProgressResponseDTO> recruiterProgress(UUID teamLeadId, UUID recruiterId) {
        if (!assignmentRepository.isRecruiterAssignedToTeamLead(teamLeadId, recruiterId)) {
            throw new AuthApiException(404, "The Recruiter is not assigned to you.");
        }
        return progressRepository.listForTeamLead(teamLeadId, recruiterId).stream()
                .map(RecruiterProgressResponseDTO::from).toList();
    }

    private AssignmentResponseDTO toAssignment(RecruiterAssignmentEntity assignment,
            Map<UUID, UserResponseDTO> users) {
        UserResponseDTO user = users.get(assignment.getRecruiterUserId());
        return new AssignmentResponseDTO(assignment.getRecruiterUserId(), user == null ? null : user.email(),
                AppRole.RECRUITER, assignment.getAssignedAt());
    }
}




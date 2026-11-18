package org.hr.recruitmen.automation.organization.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.hr.recruitmen.automation.dto.UserResponseDTO;
import org.hr.recruitmen.automation.organization.dto.AssignmentRequest;
import org.hr.recruitmen.automation.organization.dto.AssignmentResponseDTO;
import org.hr.recruitmen.automation.organization.dto.CreateStoreRequest;
import org.hr.recruitmen.automation.organization.dto.StoreResponseDTO;
import org.hr.recruitmen.automation.organization.dto.UpdateStoreRequest;
import org.hr.recruitmen.automation.organization.model.RecruiterAssignmentEntity;
import org.hr.recruitmen.automation.organization.model.StoreEntity;
import org.hr.recruitmen.automation.organization.model.TeamLeadStoreAssignmentEntity;
import org.hr.recruitmen.automation.organization.repository.AssignmentRepository;
import org.hr.recruitmen.automation.organization.repository.StoreRepository;
import org.hr.recruitmen.automation.security.AppRole;
import org.hr.recruitmen.automation.security.AuthApiException;
import org.hr.recruitmen.automation.service.SupabaseAuthService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ManagerOrganizationService {
    @Inject StoreRepository storeRepository;
    @Inject AssignmentRepository assignmentRepository;
    @Inject SupabaseAuthService authService;

    public List<StoreResponseDTO> listStores(UUID managerId) {
        return storeRepository.listActiveOwned(managerId).stream().map(StoreResponseDTO::from).toList();
    }

    public StoreResponseDTO getStore(UUID managerId, UUID storeId) {
        return StoreResponseDTO.from(requireStore(managerId, storeId));
    }

    @Transactional
    public StoreResponseDTO createStore(UUID managerId, CreateStoreRequest request) {
        String code = request.storeCode().trim().toUpperCase(Locale.ROOT);
        if (storeRepository.hasStoreCode(code)) {
            throw new AuthApiException(409, "A store with that store code already exists.");
        }
        StoreEntity store = new StoreEntity(managerId, code, request.name().trim(), clean(request.location()));
        try {
            storeRepository.persist(store);
            storeRepository.flush();
            return StoreResponseDTO.from(store);
        } catch (PersistenceException e) {
            throw new AuthApiException(409, "The store could not be created because its code is already in use.");
        }
    }

    @Transactional
    public StoreResponseDTO updateStore(UUID managerId, UUID storeId, UpdateStoreRequest request) {
        StoreEntity store = requireStore(managerId, storeId);
        store.update(request.name().trim(), clean(request.location()));
        return StoreResponseDTO.from(store);
    }

    @Transactional
    public void archiveStore(UUID managerId, UUID storeId) {
        StoreEntity store = requireStore(managerId, storeId);
        TeamLeadStoreAssignmentEntity lead = assignmentRepository.listActiveTeamLeads(storeId)
                .stream().findFirst().orElse(null);
        if (lead != null) {
            throw new AuthApiException(409, "Unassign all active team leads before archiving this store.");
        }
        store.archive();
    }

    public List<UserResponseDTO> availableManagers() {
        return authService.listUsersByRole(AppRole.MANAGER);
    }

    @Transactional
    public StoreResponseDTO transferStore(UUID currentManagerId, UUID storeId, AssignmentRequest request) {
        StoreEntity store = requireStore(currentManagerId, storeId);
        UserResponseDTO newManager = authService.requireUserRole(request.userId(), AppRole.MANAGER);
        if (newManager.userId().equals(currentManagerId)) {
            throw new AuthApiException(409, "The selected account is already the store Manager.");
        }
        store.transferTo(newManager.userId());
        return StoreResponseDTO.from(store);
    }

    public List<UserResponseDTO> availableTeamLeads() {
        return authService.listUsersByRole(AppRole.TEAM_LEAD);
    }

    public List<UserResponseDTO> availableRecruiters() {
        var assigned = assignmentRepository.listActiveRecruiterIds();
        return authService.listUsersByRole(AppRole.RECRUITER).stream()
                .filter(user -> !assigned.contains(user.userId())).toList();
    }

    public List<AssignmentResponseDTO> listTeamLeads(UUID managerId, UUID storeId) {
        requireStore(managerId, storeId);
        Map<UUID, UserResponseDTO> users = userMap(AppRole.TEAM_LEAD);
        return assignmentRepository.listActiveTeamLeads(storeId).stream()
                .map(assignment -> toAssignment(assignment.getTeamLeadUserId(), assignment.getAssignedAt(), users))
                .toList();
    }

    @Transactional
    public AssignmentResponseDTO assignTeamLead(UUID managerId, UUID storeId, AssignmentRequest request) {
        requireStore(managerId, storeId);
        UserResponseDTO teamLead = authService.requireUserRole(request.userId(), AppRole.TEAM_LEAD);
        if (assignmentRepository.findActiveTeamLead(storeId, teamLead.userId()).isPresent()) {
            throw new AuthApiException(409, "This Team Lead is already assigned to the store.");
        }
        TeamLeadStoreAssignmentEntity assignment = new TeamLeadStoreAssignmentEntity(storeId, teamLead.userId(), managerId);
        try {
            assignmentRepository.persist(assignment);
            assignmentRepository.flush();
            return new AssignmentResponseDTO(teamLead.userId(), teamLead.email(), AppRole.TEAM_LEAD, assignment.getAssignedAt());
        } catch (PersistenceException e) {
            throw new AuthApiException(409, "The Team Lead assignment conflicts with an existing assignment.");
        }
    }

    @Transactional
    public void unassignTeamLead(UUID managerId, UUID storeId, UUID teamLeadId) {
        requireStore(managerId, storeId);
        TeamLeadStoreAssignmentEntity assignment = assignmentRepository.findActiveTeamLeadForUpdate(storeId, teamLeadId)
                .orElseThrow(() -> new AuthApiException(404, "The Team Lead is not assigned to this store."));
        if (assignmentRepository.countActiveRecruiters(assignment.getId()) > 0) {
            throw new AuthApiException(409, "Unassign the Team Lead's recruiters before unassigning the Team Lead.");
        }
        assignment.unassign();
    }

    public List<AssignmentResponseDTO> listRecruiters(UUID managerId, UUID storeId, UUID teamLeadId) {
        requireStore(managerId, storeId);
        TeamLeadStoreAssignmentEntity lead = requireTeamLeadAssignment(storeId, teamLeadId);
        Map<UUID, UserResponseDTO> users = userMap(AppRole.RECRUITER);
        return assignmentRepository.listActiveRecruiters(lead.getId()).stream()
                .map(assignment -> toAssignment(assignment.getRecruiterUserId(), assignment.getAssignedAt(), users))
                .toList();
    }

    @Transactional
    public AssignmentResponseDTO assignRecruiter(UUID managerId, UUID storeId, UUID teamLeadId, AssignmentRequest request) {
        requireStore(managerId, storeId);
        TeamLeadStoreAssignmentEntity lead = assignmentRepository.findActiveTeamLeadForUpdate(storeId, teamLeadId)
                .orElseThrow(() -> new AuthApiException(404, "The Team Lead is not assigned to this store."));
        UserResponseDTO recruiter = authService.requireUserRole(request.userId(), AppRole.RECRUITER);
        if (assignmentRepository.findActiveRecruiter(recruiter.userId()).isPresent()) {
            throw new AuthApiException(409, "This Recruiter already has an active Team Lead assignment.");
        }
        RecruiterAssignmentEntity assignment = new RecruiterAssignmentEntity(lead.getId(), recruiter.userId(), managerId);
        try {
            assignmentRepository.persist(assignment);
            assignmentRepository.flush();
            return new AssignmentResponseDTO(recruiter.userId(), recruiter.email(), AppRole.RECRUITER, assignment.getAssignedAt());
        } catch (PersistenceException e) {
            throw new AuthApiException(409, "The Recruiter assignment conflicts with an existing assignment.");
        }
    }

    @Transactional
    public void unassignRecruiter(UUID managerId, UUID storeId, UUID teamLeadId, UUID recruiterId) {
        requireStore(managerId, storeId);
        TeamLeadStoreAssignmentEntity lead = assignmentRepository.findActiveTeamLeadForUpdate(storeId, teamLeadId)
                .orElseThrow(() -> new AuthApiException(404, "The Team Lead is not assigned to this store."));
        RecruiterAssignmentEntity assignment = assignmentRepository.findActiveRecruiter(lead.getId(), recruiterId)
                .orElseThrow(() -> new AuthApiException(404, "The Recruiter is not assigned to this Team Lead."));
        assignment.unassign();
    }

    private StoreEntity requireStore(UUID managerId, UUID storeId) {
        return storeRepository.findActiveOwned(storeId, managerId)
                .orElseThrow(() -> new AuthApiException(404, "Store was not found."));
    }

    private TeamLeadStoreAssignmentEntity requireTeamLeadAssignment(UUID storeId, UUID teamLeadId) {
        return assignmentRepository.findActiveTeamLead(storeId, teamLeadId)
                .orElseThrow(() -> new AuthApiException(404, "The Team Lead is not assigned to this store."));
    }

    private Map<UUID, UserResponseDTO> userMap(AppRole role) {
        Map<UUID, UserResponseDTO> users = new HashMap<>();
        authService.listUsersByRole(role).forEach(user -> users.put(user.userId(), user));
        return users;
    }

    private AssignmentResponseDTO toAssignment(UUID userId, java.time.Instant assignedAt,
            Map<UUID, UserResponseDTO> users) {
        UserResponseDTO user = users.get(userId);
        return new AssignmentResponseDTO(userId, user == null ? null : user.email(),
                user == null ? null : user.role(), assignedAt);
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}





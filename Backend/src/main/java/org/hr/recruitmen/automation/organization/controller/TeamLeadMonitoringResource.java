package org.hr.recruitmen.automation.organization.controller;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.hr.recruitmen.automation.organization.dto.AssignmentResponseDTO;
import org.hr.recruitmen.automation.organization.dto.RecruiterProgressResponseDTO;
import org.hr.recruitmen.automation.organization.dto.StoreResponseDTO;
import org.hr.recruitmen.automation.organization.service.TeamLeadMonitoringService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/team-lead")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("TEAM_LEAD")
public class TeamLeadMonitoringResource {
    @Inject TeamLeadMonitoringService monitoringService;
    @Inject JsonWebToken token;

    @GET
    @Path("/stores")
    public List<StoreResponseDTO> listStores() {
        return monitoringService.listStores(currentUserId());
    }

    @GET
    @Path("/recruiters")
    public List<AssignmentResponseDTO> listRecruiters() {
        return monitoringService.listRecruiters(currentUserId());
    }

    @GET
    @Path("/recruiters/{recruiterId}/progress")
    public List<RecruiterProgressResponseDTO> recruiterProgress(@PathParam("recruiterId") UUID recruiterId) {
        return monitoringService.recruiterProgress(currentUserId(), recruiterId);
    }

    private UUID currentUserId() { return UUID.fromString(token.getSubject()); }
}

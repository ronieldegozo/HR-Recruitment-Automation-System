package org.hr.recruitmen.automation.organization.controller;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.hr.recruitmen.automation.dto.UserResponseDTO;
import org.hr.recruitmen.automation.organization.dto.AssignmentRequest;
import org.hr.recruitmen.automation.organization.dto.AssignmentResponseDTO;
import org.hr.recruitmen.automation.organization.dto.CreateStoreRequest;
import org.hr.recruitmen.automation.organization.dto.StoreResponseDTO;
import org.hr.recruitmen.automation.organization.dto.UpdateStoreRequest;
import org.hr.recruitmen.automation.organization.service.ManagerOrganizationService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/manager")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("MANAGER")
public class ManagerOrganizationResource {
    @Inject ManagerOrganizationService organizationService;
    @Inject JsonWebToken token;

    @GET
    @Path("/stores")
    public List<StoreResponseDTO> listStores() {
        return organizationService.listStores(currentUserId());
    }

    @POST
    @Path("/stores")
    public Response createStore(@Valid CreateStoreRequest request) {
        StoreResponseDTO store = organizationService.createStore(currentUserId(), request);
        return Response.status(Response.Status.CREATED).entity(store).build();
    }

    @GET
    @Path("/stores/{storeId}")
    public StoreResponseDTO getStore(@PathParam("storeId") UUID storeId) {
        return organizationService.getStore(currentUserId(), storeId);
    }

    @PUT
    @Path("/stores/{storeId}")
    public StoreResponseDTO updateStore(@PathParam("storeId") UUID storeId, @Valid UpdateStoreRequest request) {
        return organizationService.updateStore(currentUserId(), storeId, request);
    }

    @DELETE
    @Path("/stores/{storeId}")
    public Response archiveStore(@PathParam("storeId") UUID storeId) {
        organizationService.archiveStore(currentUserId(), storeId);
        return Response.noContent().build();
    }

    @PUT
    @Path("/stores/{storeId}/manager")
    public StoreResponseDTO transferStore(@PathParam("storeId") UUID storeId,
            @Valid AssignmentRequest request) {
        return organizationService.transferStore(currentUserId(), storeId, request);
    }

    @GET
    @Path("/available-managers")
    public List<UserResponseDTO> availableManagers() {
        return organizationService.availableManagers();
    }

    @GET
    @Path("/available-team-leads")
    public List<UserResponseDTO> availableTeamLeads() {
        return organizationService.availableTeamLeads();
    }

    @GET
    @Path("/available-recruiters")
    public List<UserResponseDTO> availableRecruiters() {
        return organizationService.availableRecruiters();
    }

    @GET
    @Path("/stores/{storeId}/team-leads")
    public List<AssignmentResponseDTO> listTeamLeads(@PathParam("storeId") UUID storeId) {
        return organizationService.listTeamLeads(currentUserId(), storeId);
    }

    @POST
    @Path("/stores/{storeId}/team-leads")
    public Response assignTeamLead(@PathParam("storeId") UUID storeId, @Valid AssignmentRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(organizationService.assignTeamLead(currentUserId(), storeId, request)).build();
    }

    @DELETE
    @Path("/stores/{storeId}/team-leads/{teamLeadId}")
    public Response unassignTeamLead(@PathParam("storeId") UUID storeId,
            @PathParam("teamLeadId") UUID teamLeadId) {
        organizationService.unassignTeamLead(currentUserId(), storeId, teamLeadId);
        return Response.noContent().build();
    }

    @GET
    @Path("/stores/{storeId}/team-leads/{teamLeadId}/recruiters")
    public List<AssignmentResponseDTO> listRecruiters(@PathParam("storeId") UUID storeId,
            @PathParam("teamLeadId") UUID teamLeadId) {
        return organizationService.listRecruiters(currentUserId(), storeId, teamLeadId);
    }

    @POST
    @Path("/stores/{storeId}/team-leads/{teamLeadId}/recruiters")
    public Response assignRecruiter(@PathParam("storeId") UUID storeId,
            @PathParam("teamLeadId") UUID teamLeadId, @Valid AssignmentRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(organizationService.assignRecruiter(currentUserId(), storeId, teamLeadId, request)).build();
    }

    @DELETE
    @Path("/stores/{storeId}/team-leads/{teamLeadId}/recruiters/{recruiterId}")
    public Response unassignRecruiter(@PathParam("storeId") UUID storeId,
            @PathParam("teamLeadId") UUID teamLeadId, @PathParam("recruiterId") UUID recruiterId) {
        organizationService.unassignRecruiter(currentUserId(), storeId, teamLeadId, recruiterId);
        return Response.noContent().build();
    }

    private UUID currentUserId() {
        return UUID.fromString(token.getSubject());
    }
}


package org.hr.recruitmen.automation.organization.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.hr.recruitmen.automation.organization.dto.RecruiterProgressRequest;
import org.hr.recruitmen.automation.organization.dto.RecruiterProgressResponseDTO;
import org.hr.recruitmen.automation.organization.service.RecruiterProgressService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/recruiter/progress")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("RECRUITER")
public class RecruiterProgressResource {
    @Inject RecruiterProgressService progressService;
    @Inject JsonWebToken token;

    @GET
    public List<RecruiterProgressResponseDTO> myProgress() {
        return progressService.myProgress(currentUserId());
    }

    @PUT
    @Path("/{periodStart}")
    public RecruiterProgressResponseDTO submitProgress(@PathParam("periodStart") LocalDate periodStart,
            @Valid RecruiterProgressRequest request) {
        return progressService.upsert(currentUserId(), periodStart, request);
    }

    private UUID currentUserId() { return UUID.fromString(token.getSubject()); }
}

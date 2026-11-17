package org.hr.recruitmen.automation.controller;

import java.util.List;

import org.hr.recruitmen.automation.dto.UserResponseDTO;
import org.hr.recruitmen.automation.service.SupabaseAuthService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/admin/recruiters")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("SUPER_ADMIN")
public class AdminRecruiterResource {
    @Inject
    SupabaseAuthService authService;

    @GET
    public List<UserResponseDTO> listRecruiters() {
        return authService.listRecruiters();
    }
}
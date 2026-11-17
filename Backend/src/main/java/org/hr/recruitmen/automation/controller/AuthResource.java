package org.hr.recruitmen.automation.controller;

import org.hr.recruitmen.automation.dto.LoginRequest;
import org.hr.recruitmen.automation.dto.TokenResponseDTO;
import org.hr.recruitmen.automation.service.SupabaseAuthService;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {
    @Inject
    SupabaseAuthService authService;

    @POST
    @Path("/login")
    @PermitAll
    public TokenResponseDTO login(@Valid LoginRequest request) {
        return authService.login(request);
    }
}
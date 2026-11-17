package org.hr.recruitmen.automation.controller;

import java.util.UUID;

import org.hr.recruitmen.automation.dto.CreateUserRequest;
import org.hr.recruitmen.automation.dto.UpdateUserRoleRequest;
import org.hr.recruitmen.automation.dto.UserResponseDTO;
import org.hr.recruitmen.automation.security.AppRole;
import org.hr.recruitmen.automation.security.AuthApiException;
import org.hr.recruitmen.automation.service.SupabaseAuthService;

import org.eclipse.microprofile.jwt.JsonWebToken;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/admin/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("SUPER_ADMIN")
public class AdminUserResource {
    @Inject
    SupabaseAuthService authService;

    @Inject
    JsonWebToken token;

    @POST
    public Response createUser(@Valid CreateUserRequest request) {
        return Response.status(Response.Status.CREATED).entity(authService.createUser(request)).build();
    }

    @PUT
    @Path("/{userId}/role")
    public UserResponseDTO updateRole(@PathParam("userId") UUID userId, @Valid UpdateUserRoleRequest request) {
        if (isSelf(userId) && request.role() != AppRole.SUPER_ADMIN) {
            throw new AuthApiException(409, "You cannot remove your own Super Admin role.");
        }
        return authService.updateRole(userId, request.role());
    }

    @DELETE
    @Path("/{userId}/role")
    public UserResponseDTO removeRole(@PathParam("userId") UUID userId) {
        if (isSelf(userId)) {
            throw new AuthApiException(409, "You cannot remove your own Super Admin role.");
        }
        return authService.removeRole(userId);
    }

    @DELETE
    @Path("/{userId}")
    public Response deleteUser(@PathParam("userId") UUID userId) {
        if (isSelf(userId)) {
            throw new AuthApiException(409, "You cannot delete your own Super Admin account.");
        }
        authService.deleteUser(userId);
        return Response.noContent().build();
    }

    private boolean isSelf(UUID userId) {
        return token != null && userId.toString().equalsIgnoreCase(token.getSubject());
    }
}
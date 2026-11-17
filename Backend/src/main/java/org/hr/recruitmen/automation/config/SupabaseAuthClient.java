package org.hr.recruitmen.automation.config;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/auth/v1")
@RegisterRestClient(configKey = "supabase-auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface SupabaseAuthClient {
    @POST
    @Path("/token")
    SupabaseTokenPayload passwordGrant(
            @HeaderParam("apikey") String publishableKey,
            @QueryParam("grant_type") String grantType,
            SupabasePasswordGrantRequest request);

    @POST
    @Path("/admin/users")
    SupabaseAuthUser createUser(@HeaderParam("apikey") String secretKey,
            @HeaderParam("Authorization") String authorization, SupabaseCreateUserPayload request);

    @GET
    @Path("/admin/users")
    SupabaseAuthUserPage listUsers(@HeaderParam("apikey") String secretKey,
            @HeaderParam("Authorization") String authorization,
            @QueryParam("page") int page,
            @QueryParam("per_page") int perPage);
    @GET
    @Path("/admin/users/{userId}")
    SupabaseAuthUser getUser(@HeaderParam("apikey") String secretKey,
            @HeaderParam("Authorization") String authorization, @PathParam("userId") String userId);

    @PUT
    @Path("/admin/users/{userId}")
    SupabaseAuthUser updateUser(@HeaderParam("apikey") String secretKey,
            @HeaderParam("Authorization") String authorization, @PathParam("userId") String userId,
            SupabaseUpdateUserPayload request);

    @DELETE
    @Path("/admin/users/{userId}")
    void deleteUser(@HeaderParam("apikey") String secretKey,
            @HeaderParam("Authorization") String authorization, @PathParam("userId") String userId);
}
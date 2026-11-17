package org.hr.recruitmen.automation.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hr.recruitmen.automation.config.SupabaseAuthClient;
import org.hr.recruitmen.automation.config.SupabaseAuthUser;
import org.hr.recruitmen.automation.config.SupabaseAuthUserPage;
import org.hr.recruitmen.automation.config.SupabaseCreateUserPayload;
import org.hr.recruitmen.automation.config.SupabasePasswordGrantRequest;
import org.hr.recruitmen.automation.config.SupabaseTokenPayload;
import org.hr.recruitmen.automation.config.SupabaseUpdateUserPayload;
import org.hr.recruitmen.automation.dto.CreateUserRequest;
import org.hr.recruitmen.automation.dto.LoginRequest;
import org.hr.recruitmen.automation.dto.TokenResponseDTO;
import org.hr.recruitmen.automation.dto.UserResponseDTO;
import org.hr.recruitmen.automation.security.AppRole;
import org.hr.recruitmen.automation.security.AuthApiException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class SupabaseAuthService {
    @RestClient
    SupabaseAuthClient authClient;

    @ConfigProperty(name = "supabase.auth.publishable-key", defaultValue = "not-configured")
    String publishableKey;

    @ConfigProperty(name = "supabase.auth.admin-key", defaultValue = "not-configured")
    String secretKey;

    public TokenResponseDTO login(LoginRequest request) {
        if (isNotConfigured(publishableKey)) {
            throw new AuthApiException(503, "Supabase Auth is not configured.");
        }
        try {
            SupabaseTokenPayload token = authClient.passwordGrant(
                    publishableKey, "password",
                    new SupabasePasswordGrantRequest(request.email(), request.password()));
            AppRole role = parseRole(token.user().appMetadata() == null ? null : (String) token.user().appMetadata().get("role"));
            return new TokenResponseDTO(token.accessToken(), token.tokenType(), token.expiresIn(),
                    token.refreshToken(), token.user().id(), token.user().email(), role);
        } catch (WebApplicationException e) {
            throw new AuthApiException(401, "Email or password is incorrect.");
        } catch (AuthApiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new AuthApiException(502, "Supabase Auth is temporarily unavailable.");
        }
    }

    public List<UserResponseDTO> listRecruiters() {
        ensureAdminApiConfigured();
        try {
            List<UserResponseDTO> recruiters = new ArrayList<>();
            int page = 1;
            int perPage = 100;
            while (true) {
                SupabaseAuthUserPage userPage = authClient.listUsers(
                        secretKey, bearerSecret(), page, perPage);
                List<SupabaseAuthUser> users = userPage.users() == null ? List.of() : userPage.users();
                for (SupabaseAuthUser user : users) {
                    String role = user.appMetadata() == null
                            ? null
                            : (String) user.appMetadata().get("role");
                    if (AppRole.RECRUITER == parseRole(role)) {
                        recruiters.add(toResponse(user));
                    }
                }
                if (users.size() < perPage) {
                    return List.copyOf(recruiters);
                }
                page++;
            }
        } catch (WebApplicationException e) {
            throw mapAdminException(e, "Supabase Auth could not list recruiters.");
        } catch (AuthApiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new AuthApiException(502, "Supabase Auth is temporarily unavailable.");
        }
    }
    public UserResponseDTO createUser(CreateUserRequest request) {
        ensureAdminApiConfigured();
        try {
            SupabaseAuthUser user = authClient.createUser(secretKey, bearerSecret(),
                    new SupabaseCreateUserPayload(
                            request.email(), request.password(), true, Map.of("role", request.role().name())));
            return toResponse(user);
        } catch (WebApplicationException e) {
            int status = e.getResponse() == null ? 502 : e.getResponse().getStatus();
            if (status == 400 || status == 422) {
                throw new AuthApiException(409, "The user could not be created. Check whether the email is already registered.");
            }
            throw new AuthApiException(502, "Supabase Auth could not create the user.");
        } catch (AuthApiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new AuthApiException(502, "Supabase Auth is temporarily unavailable.");
        }
    }

    public UserResponseDTO updateRole(UUID userId, AppRole role) {
        ensureAdminApiConfigured();
        try {
            SupabaseAuthUser current = authClient.getUser(secretKey, bearerSecret(), userId.toString());
            Map<String, Object> appMetadata = copyMetadata(current);
            appMetadata.put("role", role.name());
            return toResponse(authClient.updateUser(secretKey, bearerSecret(), userId.toString(),
                    new SupabaseUpdateUserPayload(appMetadata)));
        } catch (WebApplicationException e) {
            throw mapAdminException(e, "Supabase Auth could not update the user's role.");
        } catch (AuthApiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new AuthApiException(502, "Supabase Auth is temporarily unavailable.");
        }
    }

    public UserResponseDTO removeRole(UUID userId) {
        ensureAdminApiConfigured();
        try {
            SupabaseAuthUser current = authClient.getUser(secretKey, bearerSecret(), userId.toString());
            Map<String, Object> appMetadata = copyMetadata(current);
            appMetadata.put("role", null);
            return toResponse(authClient.updateUser(secretKey, bearerSecret(), userId.toString(),
                    new SupabaseUpdateUserPayload(appMetadata)));
        } catch (WebApplicationException e) {
            throw mapAdminException(e, "Supabase Auth could not remove the user's role.");
        } catch (AuthApiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new AuthApiException(502, "Supabase Auth is temporarily unavailable.");
        }
    }

    public void deleteUser(UUID userId) {
        ensureAdminApiConfigured();
        try {
            authClient.deleteUser(secretKey, bearerSecret(), userId.toString());
        } catch (WebApplicationException e) {
            throw mapAdminException(e, "Supabase Auth could not delete the user.");
        } catch (AuthApiException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new AuthApiException(502, "Supabase Auth is temporarily unavailable.");
        }
    }

    private Map<String, Object> copyMetadata(SupabaseAuthUser user) {
        return user.appMetadata() == null ? new HashMap<>() : new HashMap<>(user.appMetadata());
    }

    private boolean isNotConfigured(String value) {
        return value == null || value.isBlank() || "not-configured".equals(value);
    }
    private void ensureAdminApiConfigured() {
        if (isNotConfigured(secretKey)) {
            throw new AuthApiException(503, "Supabase Auth Admin API is not configured.");
        }
    }

    private String bearerSecret() {
        return "Bearer " + secretKey;
    }

    private static UserResponseDTO toResponse(SupabaseAuthUser user) {
        return new UserResponseDTO(user.id(), user.email(), parseRole(
                user.appMetadata() == null ? null : (String) user.appMetadata().get("role")));
    }

    private static AppRole parseRole(String role) {
        if (role == null) {
            return null;
        }
        try {
            return AppRole.valueOf(role);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static AuthApiException mapAdminException(WebApplicationException e, String genericMessage) {
        int status = e.getResponse() == null ? 502 : e.getResponse().getStatus();
        if (status == 404) {
            return new AuthApiException(404, "User was not found.");
        }
        if (status == 400 || status == 422) {
            return new AuthApiException(400, "The requested user change is invalid.");
        }
        return new AuthApiException(502, genericMessage);
    }
}
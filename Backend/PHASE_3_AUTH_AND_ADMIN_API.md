# Phase 3: Supabase Auth and Admin API foundation

**Status: Implemented in the Backend Quarkus project; package build succeeded.** This phase adds authentication and Super Admin account administration on top of the Phase 2 backend foundation. It does not add applicant-management endpoints or change the Phase 2 read-only database access.

## What Phase 3 includes

1. **Supabase Auth login**
   - `POST /api/v1/auth/login` accepts an email and password.
   - The backend delegates password authentication to Supabase Auth.
   - The response DTO contains the access token, refresh token, token type, expiration, user ID, email, and application role.

2. **Bearer token validation and role authorization**
   - Quarkus validates Supabase-issued JWT access tokens using the project's OIDC/JWKS configuration.
   - Authorization roles come from trusted `app_metadata.role`, not user-editable metadata.
   - The initial Auth foundation supported `SUPER_ADMIN` and `RECRUITER`; Phase 2.A adds `MANAGER` and `TEAM_LEAD` as trusted app roles. See [`PHASE_2A_STORE_AND_TEAM_MANAGEMENT.md`](PHASE_2A_STORE_AND_TEAM_MANAGEMENT.md).
   - Admin routes require a valid token with the `SUPER_ADMIN` role.

3. **Super Admin-only user management**
   - `POST /api/v1/admin/users` creates a confirmed Supabase Auth user with an assigned role.
   - `GET /api/v1/admin/recruiters` lists users whose app metadata role is `RECRUITER`, returning only user ID, email, and role.
   - `PUT /api/v1/admin/users/{userId}/role` changes a user's role.
   - `DELETE /api/v1/admin/users/{userId}/role` clears the application role while retaining the Auth account.
   - `DELETE /api/v1/admin/users/{userId}` deletes the Auth account.
   - The API prevents an administrator from removing their own Super Admin role or deleting their own account.

4. **Credentials remain server-side**
   - `SUPABASE_PUBLISHABLE_KEY` is used for login.
   - `SUPABASE_SERVICE_ROLE_KEY` is required for Supabase Auth Admin operations. It must only be configured in the backend process or a secrets manager.
   - The Postgres connection continues using the dedicated `recruitflow_backend_readonly` role; do not use the `postgres` owner account for the backend.

## Local setup and API examples

See [`RUN_BACKEND.md`](RUN_BACKEND.md) for PowerShell commands to set process-local variables, run Quarkus, check database readiness, log in, and call each API route. That guide intentionally contains no passwords, tokens, or service-role key values.

## First Super Admin setup

1. Create the first Auth user in Supabase Dashboard under **Authentication → Users**.
2. From `Backend`, run `.\scripts\bootstrap-super-admin.ps1` and enter that existing account's email. If the service-role key is not already set for the PowerShell process, the helper prompts for it.
3. Sign in again through `POST /api/v1/auth/login` to get a token with the new role claim.
4. Use the access token as `Authorization: Bearer <token>` for Super Admin APIs.

The bootstrap helper only promotes an account if no Super Admin already exists. Later role assignments must use the protected API.

## Phase 3 verification and limits

- The Quarkus Maven package build completed successfully with tests skipped.
- The Phase 3 endpoints were implemented in the backend. Verify them against the running local service using the examples in `RUN_BACKEND.md` and fresh Supabase credentials.
- A successful login should return `role: SUPER_ADMIN` for the admin account.
- A role-removal response should return `role: null`; the user should then be absent from `GET /api/v1/admin/recruiters`.
- A user with their role removed can still authenticate at Supabase, but has no application role and cannot access Super Admin routes.
- Existing access tokens may keep their original claims until expiration. Have users sign in again after role changes. Do not assume a role change retroactively rewrites already-issued JWTs.
- No applicant APIs, recruiter applicant access rules, UI, or production deployment are included in this phase.

## Security follow-up

Several live credentials were pasted during development and must be treated as exposed. Rotate the Supabase database credentials and service-role key in Supabase, reset affected Auth passwords, and issue new sessions. Store runtime secrets only in local process variables or an approved secrets manager. Never commit credentials, connection strings containing passwords, access/refresh tokens, or real applicant data.

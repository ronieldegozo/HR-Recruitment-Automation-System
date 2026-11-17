# RecruitFlow Backend — Phase 2 Quarkus foundation

**Phase 2 status: Complete (2026-11-17).** The restricted Supabase connection and health checks are verified. Backend-only GitHub Actions CI is configured and passing. **Phase 3 Auth foundation: implemented locally; configure Supabase keys and bootstrap the first Super Admin before using the endpoints.**

This backend uses the Quarkus Maven project settings in this folder. The Phase 2 foundation connects to the existing Supabase Postgres database with a dedicated restricted login and performs read-only queries. n8n remains the only intake writer.

## Structure

```text
src/main/java/org/hr/recruitmen/automation/
├── model/                 # Immutable JPA entity mapping
├── repository/jpa/        # Narrow JPA query adapter
├── service/               # Read use case
├── dto/                   # Auth and admin-user API contracts
├── controller/            # Login and Super Admin account management; no applicant endpoints
├── security/              # Database/API access rules
├── config/                # Quarkus runtime configuration
└── health/                # Liveness/readiness checks

database/                  # Reference SQL and backend rules
```

Layer direction: controller -> service -> repository -> model. DTOs are HTTP boundary types and are never JPA entities. Package rules are documented in `AGENTS.md` files next to the relevant code.

## Phase 2 scope

- JPA maps the current `public.job_applications` table.
- The repository offers a count-only query; there are no applicant CRUD methods or REST resources.
- Quarkus ORM schema changes are disabled and SQL imports are turned off.
- The HTTP server binds to `127.0.0.1`; SmallRye Health reports liveness and Supabase readiness without PII.
- `pom.xml` and the existing Quarkus Maven settings are retained.

## Supabase access status

Verified in the connected Supabase project: `recruitflow_backend_readonly` exists with `LOGIN` and `NOBYPASSRLS`; it has `CONNECT` on `postgres`, `USAGE` on `public`, and `SELECT` on `public.job_applications`. RLS is enabled and the role-specific SELECT policy exists. These checks inspected role and permission metadata only, not applicant records.

The backend connection and role credentials were verified by the successful readiness check. Keep the role password in a local environment variable or managed secret only; do not put it in this repository. The postgres owner password previously shared in chat should be rotated in Supabase, and any dependent clients should be updated. The backend must use the restricted role, never the postgres owner or a service-role key.

`database/readonly-role.sql` is reference SQL only. The role, grants, and policy are already present; do not run it again as-is because it creates the policy without an existence check.

## Configure a local connection

Use the Supabase **Session Pooler** in session mode on port `5432` for this persistent JPA connection. The pooler hostname and project-specific username below are filled from the connection details you provided. Verify them in the Supabase **Connect** panel if the project connection changes. For a custom role on the shared pooler, the username includes the project reference suffix. Use SSL. `sslmode=verify-full` with the current Supabase CA certificate provides server certificate and hostname verification; `sslmode=require` encrypts traffic but does not verify the server hostname.

From a PowerShell window opened in `Backend`, set the values for that window only. The host and username are prefilled below. Enter the dedicated role password at the secure prompt; it will not be echoed:

```powershell
$env:QUARKUS_DATASOURCE_JDBC_URL = 'jdbc:postgresql://aws-0-ap-southeast-2.pooler.supabase.com:5432/postgres?sslmode=require'
$env:QUARKUS_DATASOURCE_USERNAME = 'recruitflow_backend_readonly.wnoqqpmbtlljivyxjhkq'
$secureDbPassword = Read-Host 'Supabase backend-role password' -AsSecureString
$ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureDbPassword)
try {
    $env:QUARKUS_DATASOURCE_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
}
```

These environment values disappear when that PowerShell window closes. `Backend/.env.example` is a placeholder reference; it is not loaded automatically by Quarkus. Never commit a real `.env` file or credentials.

## Run locally

From the Backend folder:

```powershell
.\mvnw.cmd quarkus:dev
```

Check liveness at `http://localhost:8080/q/health/live` and database readiness at `http://localhost:8080/q/health/ready`. Readiness returns only dependency health, never applicant fields. Stop with Ctrl+C.

If the role password, SSL, network, or pooler settings are incorrect, readiness will report the database dependency as down. The app cannot read Supabase until those values are valid.

## Safety settings

`src/main/resources/application.properties` sets ORM schema management to `none`, disables SQL imports/data scripts, and binds the HTTP server to loopback. Keep these settings when connecting to the live database. Quarkus Dev Services may use a disposable local database in development if no external datasource is supplied; never point it at Supabase with schema management enabled.

## Official references

- [Quarkus Hibernate ORM](https://quarkus.io/guides/hibernate-orm)
- [Quarkus Hibernate ORM with Panache](https://quarkus.io/guides/hibernate-orm-panache)
- [Quarkus SmallRye Health](https://quarkus.io/guides/smallrye-health)
- [Supabase Postgres connection methods](https://supabase.com/docs/guides/database/connecting-to-postgres)
- [Supabase Postgres roles](https://supabase.com/docs/guides/database/postgres/roles)

## Phase 3 Auth foundation

Supabase Auth owns HR identities and issues access and refresh tokens. Quarkus validates bearer access tokens using Supabase OIDC/JWKS and maps the trusted app_metadata role claim to application roles. Supported roles are SUPER_ADMIN and RECRUITER. The API has no public sign-up route; disable public sign-ups in Supabase Auth settings.

Routes:

- POST /api/v1/auth/login — email/password login; returns TokenResponseDTO with Supabase tokens and identity.
- POST /api/v1/admin/users — SUPER_ADMIN only; creates a confirmed email/password user with a role.
- GET /api/v1/admin/recruiters — SUPER_ADMIN only; returns all users whose app_metadata role is RECRUITER, with only ID, email, and role.
- PUT /api/v1/admin/users/{userId}/role — SUPER_ADMIN only; assigns a role.
- DELETE /api/v1/admin/users/{userId}/role — SUPER_ADMIN only; removes the role.
- DELETE /api/v1/admin/users/{userId} — SUPER_ADMIN only; deletes the Auth user.

Regular users can sign in but have no applicant or other business endpoints yet. A role change affects newly issued tokens; an already-issued JWT can remain valid until its expiry. Configure a short Supabase JWT expiry (for example, 10 minutes) while using role management, and have users sign in again after role changes.

### Required local environment variables

- RECRUITFLOW_DB_PASSWORD — password for the existing read-only database role.
- SUPABASE_URL — project URL (defaults to this repository's known project URL).
- SUPABASE_PUBLISHABLE_KEY — used for password login.
- SUPABASE_SERVICE_ROLE_KEY — server-only Supabase Auth Admin credential; required for user and role management.

The service_role credential is separate from the database password and has broad Supabase privileges. Keep it only in the backend process environment or a secret manager; never put it in the UI, Postman, or Git. Do not change the database role from read-only.

### Bootstrap the first Super Admin

1. Create the first Auth account in Supabase Dashboard > Authentication > Users with an email and strong password.
2. From the Backend directory, run .\scripts\bootstrap-super-admin.ps1. Enter the account email and provide the service_role key when prompted. The helper refuses to promote an account if any Super Admin already exists.
3. Sign in again through POST /api/v1/auth/login to receive a token containing the Super Admin role.
4. Send that access token as Authorization: Bearer <access-token> to the admin routes.

Do not put the token or service_role key in a committed Postman collection. For local Postman use, keep them in local, non-exported variables.

### Example login request

    POST http://localhost:8080/api/v1/auth/login
    Content-Type: application/json

    {
      "email": "your-admin-email",
      "password": "your-admin-password"
    }

The login result returns both accessToken and refreshToken. Treat both as credentials.

### Example create-user request

    POST http://localhost:8080/api/v1/admin/users
    Authorization: Bearer <Super Admin access token>
    Content-Type: application/json

    {
      "email": "recruiter@example.com",
      "password": "use-a-unique-password-of-at-least-12-characters",
      "role": "RECRUITER"
    }

The admin endpoint sets email_confirm=true so the user can log in with the assigned password. Password delivery to users must happen through a secure channel.

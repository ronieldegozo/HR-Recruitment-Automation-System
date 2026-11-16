# RecruitFlow Backend — Phase 2 Quarkus foundation

This backend uses the Quarkus Maven project settings in this folder. Phase 2 connects to the existing Supabase Postgres database with a dedicated restricted login and performs read-only queries. n8n remains the only intake writer.

## Structure

```text
src/main/java/org/hr/recruitmen/automation/
├── model/                 # Immutable JPA entity mapping
├── repository/jpa/        # Narrow JPA query adapter
├── service/               # Read use case
├── dto/                   # Reserved for secured API contracts
├── controller/            # Reserved; no applicant endpoints in Phase 2
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

The role's password has not been verified or changed. Set/reset it securely through Supabase's database role management UI, and never put it in this repository or chat. The `postgres` owner password previously shared in chat should be rotated in Supabase; update any client credentials that depend on it, including n8n if it uses that password. The backend must use the restricted role, never the `postgres` owner or a service-role key.

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

Check liveness at `http://127.0.0.1:8080/q/health/live` and database readiness at `http://127.0.0.1:8080/q/health/ready`. Readiness returns only dependency health, never applicant fields. Stop with Ctrl+C.

If the role password, SSL, network, or pooler settings are incorrect, readiness will report the database dependency as down. The app cannot read Supabase until those values are valid.

## Safety settings

`src/main/resources/application.properties` sets ORM schema management to `none`, disables SQL imports/data scripts, and binds the HTTP server to loopback. Keep these settings when connecting to the live database. Quarkus Dev Services may use a disposable local database in development if no external datasource is supplied; never point it at Supabase with schema management enabled.

## Official references

- [Quarkus Hibernate ORM](https://quarkus.io/guides/hibernate-orm)
- [Quarkus Hibernate ORM with Panache](https://quarkus.io/guides/hibernate-orm-panache)
- [Quarkus SmallRye Health](https://quarkus.io/guides/smallrye-health)
- [Supabase Postgres connection methods](https://supabase.com/docs/guides/database/connecting-to-postgres)
- [Supabase Postgres roles](https://supabase.com/docs/guides/database/postgres/roles)

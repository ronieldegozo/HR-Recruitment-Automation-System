# Quarkus backend architecture rules

Use these packages beneath `org.hr.recruitmen.automation`:

- `model/` — domain and JPA entity classes; no HTTP DTO concerns.
- `repository/jpa/` — persistence queries using JPA/Hibernate; Phase 2 query methods are read-only.
- `service/` — application use cases and transaction boundaries; services call repositories.
- `dto/` — explicit API request/response contracts; never serialize JPA entities directly.
- controller/ — HTTP resources; Phase 3 may expose only approved Auth and Super Admin account-management routes. Applicant endpoints remain out of scope until separately approved.
- security/ — authentication and authorization policies. App roles come only from trusted Supabase app_metadata, never user-editable metadata.
- `config/` — runtime configuration.
- `health/` — liveness/readiness checks; never disclose exception messages, secrets, or applicant data.

Dependency direction: controller -> service -> repository -> model. DTOs are only boundary types. Do not let models depend on services, repositories, or HTTP packages. Keep the existing Postgres role read-only; user administration must use Supabase Auth Admin APIs from the backend.

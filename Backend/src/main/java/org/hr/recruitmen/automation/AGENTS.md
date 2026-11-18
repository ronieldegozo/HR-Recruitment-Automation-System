# Quarkus backend architecture rules

Use these packages beneath `org.hr.recruitmen.automation`:

- `model/` — existing read-only Phase 2 JPA entities; no HTTP DTO concerns. Phase 2.A writable organization entities live under `organization/model/` and use the named organization persistence unit.
- `repository/jpa/` — persistence queries using JPA/Hibernate; Phase 2 query methods are read-only.
- `service/` — application use cases and transaction boundaries; services call repositories.
- `dto/` — explicit API request/response contracts; never serialize JPA entities directly.
- controller/ — HTTP resources; Auth and Super Admin account routes plus approved Phase 2.A organization routes live in their own packages. Applicant endpoints remain out of scope until separately approved.
- security/ — authentication and authorization policies. App roles come only from trusted Supabase app_metadata, never user-editable metadata.
- `config/` — runtime configuration.
- `health/` — liveness/readiness checks; never disclose exception messages, secrets, or applicant data.

Dependency direction: controller -> service -> repository -> model. DTOs are only boundary types. Do not let models depend on services, repositories, or HTTP packages. Keep the default Postgres role read-only; organization writes must use only the separate, least-privilege named datasource. User administration must use Supabase Auth Admin APIs from the backend.

# Quarkus backend architecture rules

Use these packages beneath `org.hr.recruitmen.automation`:

- `model/` — domain and JPA entity classes; no HTTP DTO concerns.
- `repository/jpa/` — persistence queries using JPA/Hibernate; Phase 2 query methods are read-only.
- `service/` — application use cases and transaction boundaries; services call repositories.
- `dto/` — explicit API request/response contracts; never serialize JPA entities directly.
- `controller/` — reserved for HTTP controllers/resources; no applicant routes during Phase 2.
- `security/` — authorization/authentication policies; no applicant API is exposed until these are designed.
- `config/` — runtime configuration.
- `health/` — liveness/readiness checks; never disclose exception messages, secrets, or applicant data.

Dependency direction: controller -> service -> repository -> model. DTOs are only boundary types. Do not let models depend on services, repositories, or HTTP packages.

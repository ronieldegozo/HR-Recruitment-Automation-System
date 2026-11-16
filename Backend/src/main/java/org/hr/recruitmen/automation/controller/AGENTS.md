# Controller/resource rules

- Quarkus Jakarta REST resources belong here; delegate work to services.
- Do not expose applicant data or CRUD endpoints during Phase 2.
- Do not return JPA entities; use explicit response DTOs in a later secured API phase.
- Keep the HTTP server bound to localhost until authentication and authorization are implemented.

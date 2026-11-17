# Controller/resource rules

- Quarkus Jakarta REST resources belong here; delegate work to services.
- Auth login is public; account and role administration must use SUPER_ADMIN authorization. Do not expose applicant endpoints before their own security design is implemented.
- Never return JPA entities; use explicit response DTOs.
- Keep the HTTP server bound to localhost during development; never rely on UI hiding for authorization.

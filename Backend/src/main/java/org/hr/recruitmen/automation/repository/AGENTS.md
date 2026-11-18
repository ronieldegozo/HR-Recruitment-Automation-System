# Repository rules

- Repositories own database access only; do not put business workflows or HTTP logic here.
- Existing Phase 2 applicant queries are SELECT/count only. Phase 2.A writes belong to repositories under `organization/repository/` and may touch only the `recruitflow` schema using the named datasource.
- Use JPA parameter binding for queries that accept values.
- Never log CV data or applicant PII.

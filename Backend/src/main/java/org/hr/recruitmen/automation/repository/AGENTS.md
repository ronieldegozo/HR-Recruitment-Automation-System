# Repository rules

- Repositories own database access only; do not put business workflows or HTTP logic here.
- Phase 2 queries are SELECT/count only. Do not add insert, update, or delete operations.
- Use JPA parameter binding for queries that accept values.
- Never log CV data or applicant PII.

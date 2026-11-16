# Service rules

- Services implement use cases and call repository classes; controllers do not call repositories directly.
- Keep Phase 2 database operations read-only.
- Avoid logging applicant names, emails, addresses, phone numbers, CV text, or credentials.

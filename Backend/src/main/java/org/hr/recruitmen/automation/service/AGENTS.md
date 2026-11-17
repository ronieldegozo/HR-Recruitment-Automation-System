# Service rules

- Services implement use cases and call repository classes; controllers do not call repositories directly.
- Keep Phase 2 database operations read-only.
- Avoid logging applicant names, emails, addresses, phone numbers, CV text, or credentials.

- Auth services may call Supabase Auth using the publishable key for login and the service_role credential for server-only admin operations. Never log tokens, passwords, or full Supabase error bodies.

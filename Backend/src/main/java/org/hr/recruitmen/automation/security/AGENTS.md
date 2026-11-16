# Security rules

- The Phase 2 runtime must connect with a dedicated database login, not a database owner or Supabase service-role key.
- The database role must have only the required SELECT privileges and must not have BYPASSRLS.
- Keep Row Level Security enabled and use an explicit policy scoped to the dedicated backend role.
- Do not implement applicant HTTP access before HR authentication and authorization are designed.
- Keep credentials external to source code and never emit them in logs or health responses.

# Security rules

- The Phase 2 runtime must connect with a dedicated database login, not a database owner or Supabase service-role key.
- The database role must have only the required SELECT privileges and must not have BYPASSRLS.
- Keep Row Level Security enabled and use an explicit policy scoped to the dedicated backend role.
- Supabase access tokens are verified by Quarkus OIDC; only trusted app_metadata roles grant application permissions.
- Keep credentials external to source code and never emit them in logs or health responses.

- The initial Super Admin is bootstrapped once through the local helper; never add a public self-promotion or open-registration endpoint.
- Keep the Auth Admin secret key only on the backend. Do not use the read-only Postgres password for Auth administration.

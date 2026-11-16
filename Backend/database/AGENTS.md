# Database integration rules

- Supabase is the source of truth. Phase 2 only reads the existing `public.job_applications` table.
- Keep reviewable database role/RLS SQL in this folder; do not execute it automatically.
- Use a dedicated `NOBYPASSRLS` login with only the grants this backend needs.
- Never put passwords, connection strings, access tokens, or applicant values in this folder.

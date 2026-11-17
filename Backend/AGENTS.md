# Backend implementation rules

- All backend application, database, security, configuration, and backend-specific documentation changes stay inside `Backend/`.
- Preserve the existing Quarkus Maven settings in `pom.xml` unless the user explicitly asks to change them.
- Keep Phase 2 applicant database access read-only against the existing Supabase public.job_applications table. Phase 3 may add Supabase Auth login and Super Admin-only account/role management through the Auth Admin API. Do not add applicant endpoints until their authorization design is approved.
- Keep credentials in environment variables or a secrets manager. Never commit passwords, tokens, connection strings, publishable/admin keys, or real applicant data. Supabase Auth administrator credentials are server-only and must never be returned by the API.
- Keep the layers separate. Follow `src/main/java/org/hr/recruitmen/automation/AGENTS.md` and the package-level instructions.
- Database-role and RLS setup SQL belongs in `database/` and is review-only unless the user explicitly asks for a live database change.
- Do not modify project folders outside `Backend/` for backend implementation work.

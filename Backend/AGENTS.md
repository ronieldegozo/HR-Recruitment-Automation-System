# Backend implementation rules

- All backend application, database, security, configuration, and backend-specific documentation changes stay inside `Backend/`.
- Preserve the existing Quarkus Maven settings in `pom.xml` unless the user explicitly asks to change them.
- Phase 2 is read-only against the existing Supabase `public.job_applications` table. Do not add writes, schema creation/migration, or applicant HTTP endpoints.
- Keep credentials in environment variables or a secrets manager. Never commit passwords, tokens, connection strings, or real applicant data.
- Keep the layers separate. Follow `src/main/java/org/hr/recruitmen/automation/AGENTS.md` and the package-level instructions.
- Database-role and RLS setup SQL belongs in `database/` and is review-only unless the user explicitly asks for a live database change.
- Do not modify project folders outside `Backend/` for backend implementation work.

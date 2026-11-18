# Database access boundary

Phase 2 maps the existing Supabase `public.job_applications` table and exposes no applicant API. Supabase currently has RLS enabled on that table and no policies. A SELECT grant alone is not enough; the backend login requires a targeted RLS SELECT policy.

`readonly-role.sql` is prepared for an administrator to review and apply after creating the dedicated role. It has not been executed. Do not use the Supabase database owner or service-role key as the backend credential.

## Phase 2.A organization schema

`phase_2a_organization.sql` defines the separate private `recruitflow` schema. The owner reports the migration has now been run in Supabase. The dedicated `recruitflow_backend_org` login should have `NOBYPASSRLS`, no schema ownership, and a strong password outside Git. It must not inherit `service_role` or `postgres`. Configure `RECRUITFLOW_ORG_DB_ENABLED`, `RECRUITFLOW_ORG_DB_USERNAME`, and `RECRUITFLOW_ORG_DB_PASSWORD` locally or in Render. The existing read-only role and `public.job_applications` privileges are unchanged.

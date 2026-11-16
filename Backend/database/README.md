# Database access boundary

Phase 2 maps the existing Supabase `public.job_applications` table and exposes no applicant API. Supabase currently has RLS enabled on that table and no policies. A SELECT grant alone is not enough; the backend login requires a targeted RLS SELECT policy.

`readonly-role.sql` is prepared for an administrator to review and apply after creating the dedicated role. It has not been executed. Do not use the Supabase database owner or service-role key as the backend credential.

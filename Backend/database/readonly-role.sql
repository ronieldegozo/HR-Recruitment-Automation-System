-- Reference SQL only. The role, grants, and SELECT policy have been verified
-- as already present in the connected Supabase project.
-- Do not run this file again as-is: CREATE POLICY is not idempotent.
-- Set or rotate the role password through Supabase role management, not here.
GRANT CONNECT ON DATABASE postgres TO recruitflow_backend_readonly;
GRANT USAGE ON SCHEMA public TO recruitflow_backend_readonly;
GRANT SELECT ON TABLE public.job_applications TO recruitflow_backend_readonly;

CREATE POLICY recruitflow_backend_readonly_select
    ON public.job_applications
    FOR SELECT
    TO recruitflow_backend_readonly
    USING (true);

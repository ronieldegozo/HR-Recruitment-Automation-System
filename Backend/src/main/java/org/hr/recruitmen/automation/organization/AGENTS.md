# Organization API rules

- Keep Phase 2.A code in this `organization/` package tree. Preserve the existing Phase 2 `public.job_applications` model and read-only datasource.
- Use the named `organization` datasource and persistence unit only for `recruitflow` tables. Never route organization writes through the default read-only datasource.
- Role claims come only from trusted Supabase `app_metadata.role`. Check the target account's Supabase Auth role before assigning it.
- Every manager operation must scope stores to the caller's subject UUID. Team Leads may see only their active assignments and their assigned recruiters' progress.
- Do not return JPA entities. Use request/response DTOs. Repositories own persistence; services enforce access and business rules; REST resources enforce role gates.
- Recruiter progress in Phase 2.A is self-reported aggregate data. Do not expose or mutate applicant records in this module.
- Schema SQL is reference-only and must not be executed against Supabase without an explicit user request.

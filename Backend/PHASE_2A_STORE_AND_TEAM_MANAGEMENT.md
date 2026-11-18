# Phase 2.A — Store, team, and recruiter progress APIs

**Status:** Implemented in Backend. The owner reports that the organization database role/migration were set up and local store creation plus Manager → Team Lead → Recruiter assignments succeeded. Remaining progress-report/authorization cases and Live deployment still need verification. The new APIs return `503` when the separate organization datasource is disabled or not configured.

## Goal

Let Managers manage their own stores and reporting assignments, and let Team Leads view their assigned stores, recruiters, and recruiter-submitted progress reports. This enhancement is separate from applicant records and does not modify the Phase 1 table or n8n workflow.

## Role boundaries

| Role | Phase 2.A access |
| --- | --- |
| `SUPER_ADMIN` | Creates accounts and assigns trusted roles through existing admin APIs. Does not make ordinary store assignments. |
| `MANAGER` | Creates, reads, updates, and archives stores they own; assigns existing `TEAM_LEAD` and `RECRUITER` accounts in their store hierarchy. |
| `TEAM_LEAD` | Views stores and recruiters currently assigned to them; views progress reports from those recruiters. |
| `RECRUITER` | Submits and views their own progress reports while they have an active Team Lead assignment. |

The Manager's user ID comes from the verified access token; a request cannot choose another owner. Assignments validate the target user's trusted Supabase `app_metadata.role`. Recruiters have one active Team Lead reporting line. Once a user has active store/team links, the Super Admin account API requires those links to be removed before the account role can change or the account can be deleted. A Team Lead can be assigned to more than one store. All manager queries are scoped by the authenticated Manager's ID.

## API routes

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| GET | `/api/v1/manager/stores` | Manager | List the caller's active stores |
| POST | `/api/v1/manager/stores` | Manager | Create a store; owner is taken from the token |
| GET | `/api/v1/manager/stores/{storeId}` | Manager | Read one owned store |
| PUT | `/api/v1/manager/stores/{storeId}` | Manager | Update its name/location |
| DELETE | `/api/v1/manager/stores/{storeId}` | Manager | Archive an empty store; unassign Team Leads first |
| GET | `/api/v1/manager/available-managers` | Manager | List Manager accounts for ownership transfer |
| PUT | `/api/v1/manager/stores/{storeId}/manager` | Manager | Transfer an owned store to another Manager |
| GET | `/api/v1/manager/available-team-leads` | Manager | List accounts with the Team Lead role |
| GET | `/api/v1/manager/available-recruiters` | Manager | List Recruiters without an active assignment |
| GET/POST | `/api/v1/manager/stores/{storeId}/team-leads` | Manager | List or assign a Team Lead |
| DELETE | `/api/v1/manager/stores/{storeId}/team-leads/{teamLeadId}` | Manager | Unassign a Team Lead after its recruiters are unassigned |
| GET/POST | `/api/v1/manager/stores/{storeId}/team-leads/{teamLeadId}/recruiters` | Manager | List or assign a Recruiter to that Team Lead |
| DELETE | `/api/v1/manager/stores/{storeId}/team-leads/{teamLeadId}/recruiters/{recruiterId}` | Manager | Unassign a Recruiter |
| GET | `/api/v1/team-lead/stores` | Team Lead | List currently assigned stores |
| GET | `/api/v1/team-lead/recruiters` | Team Lead | List currently assigned Recruiters |
| GET | `/api/v1/team-lead/recruiters/{recruiterId}/progress` | Team Lead | View that Recruiter's progress history |
| GET | `/api/v1/recruiter/progress` | Recruiter | View own progress reports |
| PUT | `/api/v1/recruiter/progress/{periodStart}` | Recruiter | Submit or update a self-reported period summary |

Store deletion is an archive, not a hard delete. Assignment history is retained. Store ownership can be transferred by the current owner to another Manager; the new owner then controls that store and its assignment APIs. Existing Super Admin account-management routes remain protected by `SUPER_ADMIN`; the new routes are protected by their specific role.

## Progress data boundary

Progress reports currently hold aggregate, self-reported counts for candidates reviewed, candidates contacted, and interviews scheduled, plus optional notes. They are not verified against applicant records. The Phase 1 `public.job_applications` table has no recruiter ownership/assignment link, and Phase 2.A intentionally does not add one. A later recruitment API phase can replace or complement these reports with system-derived activity once candidate assignment is designed.

Headcount caps and requests are not part of this API slice. Service crew/employee records must remain separate from Recruiter login accounts. Headcount enforcement should be designed as a later workforce feature before n8n begins automating those requests.

## Database isolation

- `Backend/database/phase_2a_organization.sql` creates a new private `recruitflow` schema for stores, assignments, and progress reports.
- The existing default datasource and `public.job_applications` read-only mapping remain unchanged.
- Organization reads/writes use the named `organization` datasource and separate `recruitflow_backend_org` login. This role should have `NOBYPASSRLS`, no schema ownership, and access only to the `recruitflow` schema. It must not inherit `service_role` or `postgres` privileges.
- Tables enable and force RLS. The schema is not exposed to the Data API; the backend applies caller/store scoping. Keep database credentials server-side.
- Hibernate schema generation remains disabled. The SQL migration is maintained as a manual database operation; the owner reports it has now been applied to Supabase.

To enable the APIs after setting up the role and applying the reviewed schema, configure these process/Render variables:

- `RECRUITFLOW_ORG_DB_ENABLED=true`
- `RECRUITFLOW_ORG_DB_USERNAME` (default username format is configured in `application.properties`)
- `RECRUITFLOW_ORG_DB_PASSWORD`
- `RECRUITFLOW_ORG_DB_JDBC_URL` only if the project connection endpoint changes

When `RECRUITFLOW_ORG_DB_ENABLED` is false or missing, the organization persistence unit stays inactive, the existing read-only connection continues to work, and organization routes return a safe `503` response. `/q/health/ready` reports the new dependency as disabled/UP; when enabled, it checks the organization connection separately.

## Local setup and verification

1. Review the SQL and provision the separate database login with a strong password outside Git.
2. Apply `phase_2a_organization.sql` manually only after reviewing it in Supabase.
3. In the PowerShell process running Quarkus, set `RECRUITFLOW_ORG_DB_ENABLED=true` and the role password; use `Backend/RUN_BACKEND.md` for the environment setup pattern.
4. Assign `MANAGER`, `TEAM_LEAD`, and `RECRUITER` roles to test Auth users through the existing Super Admin API, then have each user sign in again for a fresh role claim.
5. Import the updated Postman collection/environment. Use the nested **Phase 2.A - Store & Team Management** folder inside Local or Live.
6. Verify ownership isolation, invalid target-role rejection, assignment history, archive guards, progress visibility, and 503 behavior while the organization datasource is disabled.

## Not included

- Headcount limit, workforce employee records, or approval requests.
- Applicant/application CRUD or changes to Phase 1's n8n workflow.
- n8n notifications or automated progress reminders.
- A UI or production database migration.


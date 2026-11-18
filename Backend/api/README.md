# HR Recruiter WF API calls

The Postman collection contains equivalent **Local** and **Live** folders. Each folder includes health checks, login, recruiter listing, user creation, role change/removal, and permanent account deletion.

## Import

1. In Postman, use **Import** and select `HR Recruiter WF.postman_collection.json`.
2. Import `HR Recruiter WF.postman_environment.json` as an environment.
3. Select the imported `HR Recruiter WF - Shared` environment from the environment picker.
4. In the environment editor, fill the local and/or live admin email and password values. Keep these values in Postman's local environment storage; do not export or commit a populated environment.
5. Choose **Local** or **Live** in the collection and run **Login** first. A successful login saves its access token automatically in the corresponding `localAccessToken` or `liveAccessToken` environment variable.
6. Run the other requests from that same folder. The URL and bearer token are already connected to its variables.

The environment template intentionally contains blank credential and token values. Enter credentials once in Postman; do not paste tokens into request authorization fields or edit URLs per request. Local calls use `http://localhost:8080`. Live calls use the Render service URL currently configured in the collection/environment; update that variable if the service address changes.

## Inputs

- `newUserEmail` and `newUserPassword`: values for Create User. The backend requires a password of at least 12 characters.
- `targetUserId`: UUID used by role and delete requests.
- `newRole`: `SUPER_ADMIN`, `MANAGER`, `TEAM_LEAD`, or `RECRUITER`.

Admin requests require a `SUPER_ADMIN` bearer token. The role and account deletion operations are consequential. Delete User permanently deletes the Auth account and sends no request body. The API does not allow the current Super Admin to remove their own admin role or delete their own account.

## Included endpoints

| Method | Path | Access |
| --- | --- | --- |
| GET | `/q/health/live` | Public health |
| GET | `/q/health/ready` | Public readiness/database check |
| POST | `/api/v1/auth/login` | Public login |
| GET | `/api/v1/admin/recruiters` | Super Admin |
| POST | `/api/v1/admin/users` | Super Admin |
| PUT | `/api/v1/admin/users/{userId}/role` | Super Admin |
| DELETE | `/api/v1/admin/users/{userId}/role` | Super Admin |
| DELETE | `/api/v1/admin/users/{userId}` | Super Admin; permanent account deletion |

When adding or changing backend endpoints, follow `AGENTS.md` in this folder and keep Local and Live requests synchronized.

## Phase 2.A store and team APIs

The **Phase 2.A - Store & Team Management** nested folder appears under both **Local** and **Live**. Use the role-specific Login requests first; they save role-specific tokens automatically. Configure the blank Manager, Team Lead, and Recruiter environment credentials once in Postman. Managers use the manager requests, Team Leads use their roster/progress requests, and Recruiters use their own progress requests.

The organization APIs require the reviewed `Backend/database/phase_2a_organization.sql` schema and a separate least-privilege database login. Until the `RECRUITFLOW_ORG_DB_ENABLED`, `RECRUITFLOW_ORG_DB_USERNAME`, and `RECRUITFLOW_ORG_DB_PASSWORD` server settings are in place, organization requests return `503`. See `Backend/PHASE_2A_STORE_AND_TEAM_MANAGEMENT.md`. Recruiter progress is self-reported aggregate data; it is not calculated from applicant records.

| Method | Path | Access |
| --- | --- | --- |
| GET/POST | `/api/v1/manager/stores` | Manager |
| GET/PUT/DELETE | `/api/v1/manager/stores/{storeId}` | Manager; DELETE archives |
| GET | `/api/v1/manager/available-team-leads` | Manager |
| GET | `/api/v1/manager/available-recruiters` | Manager |
| GET/POST | `/api/v1/manager/stores/{storeId}/team-leads` | Manager |
| DELETE | `/api/v1/manager/stores/{storeId}/team-leads/{teamLeadId}` | Manager |
| GET/POST | `/api/v1/manager/stores/{storeId}/team-leads/{teamLeadId}/recruiters` | Manager |
| DELETE | `/api/v1/manager/stores/{storeId}/team-leads/{teamLeadId}/recruiters/{recruiterId}` | Manager |
| GET | `/api/v1/team-lead/stores` | Team Lead |
| GET | `/api/v1/team-lead/recruiters` | Team Lead |
| GET | `/api/v1/team-lead/recruiters/{recruiterId}/progress` | Team Lead; assigned Recruiters only |
| GET/PUT | `/api/v1/recruiter/progress` and `/api/v1/recruiter/progress/{periodStart}` | Recruiter; own reports |

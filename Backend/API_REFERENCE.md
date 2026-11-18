# RecruitFlow Backend API Reference

This is the working API catalog for the backend and the UI handoff. Update it whenever a route, payload, authorization rule, or response changes. Keep the Local and Live folders in `api/HR Recruiter WF.postman_collection.json` synchronized with the same change.

## Current status

- Phase 2.A database migration and organization database connection: owner reports setup succeeded.
- Local store creation and Manager → Team Lead → Recruiter assignments: owner reports successful calls and verified database rows.
- Recruiter progress submission/Team Lead viewing, every error case, and the Render deployment: still need separate verification.
- Applicant CRUD/assignment APIs do not exist yet. The Recruiter progress values below are self-reported and are not derived from applicant records.

## Base URLs

Use the same paths below with either base URL:

| Target | Base URL |
|---|---|
| Local | `http://localhost:8080` |
| Live (Render) | `https://hr-recruitment-automation-system.onrender.com` |

The Live URL only supports these routes after the matching backend version and required Render environment variables have been deployed.

## Authentication and roles

Send JSON requests with `Content-Type: application/json`. Protected requests also need:

```http
Authorization: Bearer <access-token>
```

Sign in to get a token. Admin endpoints require `SUPER_ADMIN`; manager endpoints require `MANAGER`; Team Lead endpoints require `TEAM_LEAD`; Recruiter progress endpoints require `RECRUITER`. Sign in again after a role change to get a token with the new role claim.

## Health

| Method | Path | Access | Description |
|---|---|---|---|
| GET | `/q/health/live` | Public | Confirms the Quarkus process is alive. |
| GET | `/q/health/ready` | Public | Shows whether required configured dependencies, including Postgres, are ready. |

Example:

```http
GET http://localhost:8080/q/health/ready
```

For Live, use `https://hr-recruitment-automation-system.onrender.com/q/health/ready`.

When the organization datasource is disabled, Phase 2.A routes return `503`. Before testing them, configure `RECRUITFLOW_ORG_DB_ENABLED=true`, `RECRUITFLOW_ORG_DB_USERNAME`, and `RECRUITFLOW_ORG_DB_PASSWORD` for the backend process; the migration must also exist in Supabase. Never put database passwords in Postman or this document.

## Start the local API

1. Open PowerShell in `Backend` and set the database/Auth environment variables from `RUN_BACKEND.md`.
2. To use the Phase 2.A store APIs, set `RECRUITFLOW_ORG_DB_ENABLED=true` and provide the organization role username/password as described in `RUN_BACKEND.md`.
3. Start Quarkus with `.\mvnw.cmd quarkus:dev` and check `http://localhost:8080/q/health/ready` before making protected API calls.

## Authentication and Super Admin user management

### Sign in

**POST** `/api/v1/auth/login` — verifies the user's Supabase Auth credentials and returns access/refresh tokens and the application role.

```json
{
  "email": "manager@example.com",
  "password": "<user-password>"
}
```

Use the returned `accessToken` as the Bearer token. Login passwords are not returned or stored by this API.

Success response fields: `accessToken`, `tokenType`, `expiresIn`, `refreshToken`, `userId`, `email`, and `role`.

### List Recruiters

**GET** `/api/v1/admin/recruiters` — lists Auth users with the `RECRUITER` role.

Requires a Super Admin token. No request body.

### Create a user

**POST** `/api/v1/admin/users` — creates a confirmed Auth user with the selected application role. Password must be 12–128 characters.

Requires a Super Admin token.

```json
{
  "email": "teamlead@example.com",
  "password": "<unique-password-at-least-12-characters>",
  "role": "TEAM_LEAD"
}
```

Allowed roles: `SUPER_ADMIN`, `MANAGER`, `TEAM_LEAD`, `RECRUITER`.

### Change a user's role

**PUT** `/api/v1/admin/users/{userId}/role` — changes the user's trusted application role.

Requires a Super Admin token.

```json
{
  "role": "MANAGER"
}
```

### Remove a user's application role

**DELETE** `/api/v1/admin/users/{userId}/role` — clears the role but leaves the Supabase Auth account in place. Requires a Super Admin token and has no request body. Active store/team assignments must be removed first.

### Delete a user account

**DELETE** `/api/v1/admin/users/{userId}` — permanently deletes the Supabase Auth account. Requires a Super Admin token and has no request body. Active store/team assignments must be removed first.

## Manager store and assignment APIs

All routes in this section require a Manager token. The Manager ID is taken from the verified token; callers cannot set another owner in the create-store payload.

### Stores

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/manager/stores` | Lists the caller's active stores. |
| POST | `/api/v1/manager/stores` | Creates a store owned by the caller. |
| GET | `/api/v1/manager/stores/{storeId}` | Gets one store owned by the caller. |
| PUT | `/api/v1/manager/stores/{storeId}` | Updates the store name and location. |
| DELETE | `/api/v1/manager/stores/{storeId}` | Archives the store; it does not hard-delete it. Unassign its Team Leads first. |
| GET | `/api/v1/manager/available-managers` | Lists Manager accounts for ownership transfer. |
| PUT | `/api/v1/manager/stores/{storeId}/manager` | Transfers an owned store to another Manager. |

Create store — **POST** `/api/v1/manager/stores`:

```json
{
  "storeCode": "BK-MNL-001",
  "name": "Burger King Manila",
  "location": "Manila"
}
```

The response includes `storeId`, `storeCode`, `name`, `location`, `status`, `createdAt`, and `updatedAt`. Save `storeId` for the assignment calls.

Update store — **PUT** `/api/v1/manager/stores/{storeId}`:

```json
{
  "name": "Burger King Manila - North",
  "location": "Quezon City"
}
```

Transfer ownership — **PUT** `/api/v1/manager/stores/{storeId}/manager`:

```json
{
  "userId": "<another-manager-user-id>"
}
```

### Team Lead assignments

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/manager/available-team-leads` | Lists users with the `TEAM_LEAD` role. |
| GET | `/api/v1/manager/stores/{storeId}/team-leads` | Lists Team Leads assigned to the owned store. |
| POST | `/api/v1/manager/stores/{storeId}/team-leads` | Assigns a Team Lead to the owned store. |
| DELETE | `/api/v1/manager/stores/{storeId}/team-leads/{teamLeadId}` | Unassigns a Team Lead. Unassign that lead's Recruiters first. |

Assign — **POST** `/api/v1/manager/stores/{storeId}/team-leads`:

```json
{
  "userId": "<team-lead-user-id>"
}
```

Assignment responses include `userId`, `email`, `role`, and `assignedAt`.

### Recruiter assignments

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/manager/available-recruiters` | Lists Recruiters without an active Team Lead assignment. |
| GET | `/api/v1/manager/stores/{storeId}/team-leads/{teamLeadId}/recruiters` | Lists Recruiters assigned under that store's Team Lead. |
| POST | `/api/v1/manager/stores/{storeId}/team-leads/{teamLeadId}/recruiters` | Assigns a Recruiter under the Team Lead already assigned to that store. |
| DELETE | `/api/v1/manager/stores/{storeId}/team-leads/{teamLeadId}/recruiters/{recruiterId}` | Unassigns a Recruiter. |

Assign — **POST** `/api/v1/manager/stores/{storeId}/team-leads/{teamLeadId}/recruiters`:

```json
{
  "userId": "<recruiter-user-id>"
}
```

Assignment responses include `userId`, `email`, `role`, and `assignedAt`.

The flow is **Manager → Store → Team Lead → Recruiter**. Assign the Team Lead to the store before assigning a Recruiter to that Team Lead. A Recruiter can have only one active Team Lead assignment.

## Team Lead monitoring APIs

All routes require the signed-in Team Lead's token. Results are limited to that Team Lead's active assignments.

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/team-lead/stores` | Lists stores currently assigned to the Team Lead. |
| GET | `/api/v1/team-lead/recruiters` | Lists Recruiters currently assigned to the Team Lead. |
| GET | `/api/v1/team-lead/recruiters/{recruiterId}/progress` | Views progress reports for an assigned Recruiter. |

No request bodies are required for these calls.

## Recruiter progress APIs

These routes require the Recruiter's own token. Progress is self-reported aggregate data; it is not yet calculated from applicant/application records.

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/recruiter/progress` | Lists the signed-in Recruiter's progress reports. |
| PUT | `/api/v1/recruiter/progress/{periodStart}` | Creates or updates the report for the start date in `YYYY-MM-DD` format. Requires an active Team Lead assignment. |

Example — **PUT** `/api/v1/recruiter/progress/2026-11-01`:

```json
{
  "periodEnd": "2026-11-07",
  "candidatesReviewed": 12,
  "candidatesContacted": 8,
  "interviewsScheduled": 3,
  "notes": "Weekly progress summary"
}
```

The response includes `reportId`, `recruiterUserId`, `periodStart`, `periodEnd`, the three submitted counts, `notes`, `createdAt`, and `updatedAt`.

Counts must be zero or greater; `periodEnd` cannot be before `periodStart` or in the future.

## Example end-to-end request sequence

Use these IDs from the API responses; do not literally include angle brackets in a URL.

1. Super Admin signs in, creates the Manager, Team Lead, and Recruiter accounts, each with the matching `role`.
2. Manager signs in and calls `POST /api/v1/manager/stores`.
3. Manager calls `POST /api/v1/manager/stores/{storeId}/team-leads` with the Team Lead `userId`.
4. Manager calls `POST /api/v1/manager/stores/{storeId}/team-leads/{teamLeadId}/recruiters` with the Recruiter `userId`.
5. Team Lead signs in and calls `/api/v1/team-lead/stores`, `/api/v1/team-lead/recruiters`, and the Recruiter progress endpoint.
6. Recruiter signs in, submits a report with `PUT /api/v1/recruiter/progress/{periodStart}`, and can review it with `GET /api/v1/recruiter/progress`.

## API update and review rules

- Add every new endpoint and update every changed endpoint in this file in the same change as the implementation.
- For each endpoint, record its HTTP method, full path, short purpose, required role, request payload (or say no body), and important constraints/side effects.
- Add or update the matching request in both **Local** and **Live** folders of `api/HR Recruiter WF.postman_collection.json`; update `api/HR Recruiter WF.postman_environment.json` only with blank, non-secret variables if needed.
- Keep sample data fictional. Never add passwords, access/refresh tokens, database credentials, service-role keys, or real applicant data.
- If a UI depends on an endpoint, link the UI feature to this catalog entry and note when the endpoint is not yet deployed to Live.
- Before calling an API complete, verify its success response, role authorization, invalid input behavior, and persistence/ownership boundaries. Record unverified items in **Current status** above.


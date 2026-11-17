# RecruitFlow --- Development Execution Plan

> **Purpose:** This is the working development checklist for building
> RecruitFlow phase by phase. Complete the tasks and acceptance criteria
> in each phase before moving to the next one.
>
> **Recommended approach:** Phase 1 now handles applicant email intake,
> CV validation, AI extraction, Supabase persistence, and receipt
> acknowledgments. Next, build the backend foundation around that existing
> Supabase database, add secure recruitment APIs, then route n8n through
> the backend before building the HR dashboard.

------------------------------------------------------------------------

## Project goal

Build a single-company HR Recruitment Management System that:

-   Receives applicant CVs through a Gmail inbox.
-   Automatically acknowledges valid applications.
-   Extracts CV text from PDF and DOCX files.
-   Stores applicant and application information.
-   Assists HR with job-specific CV assessments.
-   Lets HR review candidates and manage recruitment statuses.
-   Reminds HR when applications have not been reviewed or contacted.

**Important:** AI assists HR. It must not make final hiring or rejection
decisions.

## Current architecture after Phase 1

```mermaid
flowchart LR
  A[Applicant emails CV] --> B[Gmail recruitment inbox]
  B --> C[n8n validates sender and PDF/DOCX]
  C --> D[LlamaParse extracts CV text]
  D --> E[OpenAI GPT-4.1 extracts stated facts]
  E --> F[Normalize structured fields]
  F --> G[(Supabase Postgres<br/>public.job_applications)]
  C -. duplicate guard and receipt .-> H[Gmail acknowledgment<br/>24–48 hour expectation]
  H --> B
```

Phase 1's current n8n workflow writes directly to Supabase Postgres. The
planned architecture keeps that database as the single source of truth.
In Phase 4, after the backend API is secure and ready, n8n will send
application data through Quarkus, which will write to the same
Supabase database. The frontend will use the authenticated backend API;
it will not connect with database passwords or privileged keys.

## Technology stack

  Area                  Technology
  --------------------- ----------------------------------------------------
  Workflow automation   n8n Cloud (current Phase 1 instance)
  Email                 Gmail API / Gmail OAuth
  Backend               Java 25 / Quarkus 3.40.1.2
  Database              Supabase Postgres (single source of truth)
  Frontend              Next.js / TypeScript / Tailwind CSS
  CV parsing            LlamaParse (current); evaluate Tika only if needed
  AI                    OpenAI GPT-4.1 for Phase 1 extraction; job assessment later
  API testing           Postman
  Source control        Git / GitHub
  Local environment     Docker / Docker Compose

------------------------------------------------------------------------

# Phase 0 --- Project preparation

**Goal:** Prepare the development environment and repository.

### Tasks

-   [ ] Create a GitHub repository named `recruitflow`.
-   [ ] Create the initial folder structure.
-   [ ] Confirm Java 25 is configured for the Quarkus Maven Wrapper.
-   [ ] Install/check Node.js and npm.
-   [ ] Confirm Supabase Postgres project access and database configuration.
-   [ ] Install Docker Desktop.
-   [ ] Decide whether to run n8n with Docker or npm.
-   [ ] Create a project `.gitignore`.
-   [ ] Create a local environment configuration file (do not commit
    secrets).
-   [ ] Add a basic README with setup instructions.

### Suggested structure

``` text
recruitflow/
├── frontend/
├── backend/
├── automation/
│   └── n8n-workflows/
├── database/
│   └── migrations/
├── docs/
│   ├── architecture/
│   └── development/
├── docker-compose.yml
├── .gitignore
└── README.md
```

### Acceptance criteria

-   [ ] Repository exists and initial commit is pushed.
-   [ ] Required development tools are installed.
-   [ ] Folder structure is committed.
-   [ ] Secrets and local environment files are excluded from Git.

------------------------------------------------------------------------

# Phase 1 --- Gmail + n8n + Supabase applicant intake

**Status: Complete — owner confirmed the end-to-end intake, acknowledgment, Supabase save, and AI extraction are working (2026-11-10).**

**Phase 1 goal:** An applicant emails a CV to the recruitment Gmail inbox. n8n validates the message and CV, extracts available applicant details with AI, saves the application record to Supabase Postgres, and sends an automated receipt confirmation with a 24–48-hour contact expectation. The records are ready for HR review in a later phase; a dashboard and HR status management are not part of Phase 1.

This is the implemented Phase 1 scope, expanded from the original Gmail-only proof of concept. The current workflow is the n8n Cloud workflow **HR Recruitment Automation - WF**.

## Implemented workflow

1. Gmail Trigger detects messages sent to the recruitment inbox.
2. n8n fetches the full message and its attachment, then checks for a sender and an allowed PDF or DOCX CV (up to 10 MB).
3. LlamaParse reads the CV and the OpenAI node returns structured fields when they can be found, including applicant name, mobile number, address, and work experience.
4. A parameterized Postgres query writes or updates the application in Supabase table public.job_applications, using the Gmail message ID to avoid duplicate application rows.
5. A Gmail acknowledgment is configured with the subject **Application Received | People Serve MPC** and tells the applicant the team aims to contact them within 24–48 hours.

## Workflow implementation notes

- [x] Gmail Trigger has received multiple applicant messages in one execution.
- [x] The workflow kept the applicants and their CV attachments paired in execution 111.
- [x] CV extraction ran for both applicants in that execution.
- [x] Both application records were saved to Supabase.
- [x] The Merge node is configured to combine by position.
- [x] The acknowledgment routing fix is published: the Data Table lookup sets explicit ack_exists and should_acknowledge flags before the Merge, so Normalize can carry them through to Postgres.
- [x] The workflow canvas includes a published Phase 1 summary note covering intake, AI extraction, Supabase persistence, and acknowledgments.

## Completion record

- [x] Owner confirms applicants email a CV, receive an automatic acknowledgment with a 24–48-hour contact expectation, and application information is saved in Supabase Postgres.
- [x] Owner confirms the AI workflow extracts applicant information and experience.
- [x] Phase 1 marked complete on 2026-11-10 based on the owner's end-to-end completion report.

**Evidence note:** The prior execution notes in this document recorded that execution 111 predated the latest acknowledgment-routing change and did not run the Gmail send node. The owner has since confirmed Phase 1 is working. Keep this historical note as context; no new execution or mailbox evidence was provided in this update.

## Data and scope notes

- Supabase Postgres is the current Phase 1 database. The initial table is public.job_applications.
- resume_files currently stores attachment metadata (such as filename, MIME type, and size), not the actual PDF/DOCX file. The original CV remains in Gmail; Supabase Storage has not been connected for CV file storage.
- The automated acknowledgment is a receipt confirmation. It does not perform the HR team's actual 24–48-hour follow-up.
- AI extraction is assistive and may require manual review. It does not decide whether to hire or reject an applicant.
- The Quarkus backend, HR dashboard, job-specific assessment, applicant status management, and scheduled HR reminders remain later-phase work.
- The original Gmail-only POC guide is historical setup material; this section describes the current integrated workflow.

**Phase 1 deliverable:** The working n8n workflow that receives applicant CVs, stores application information in Supabase, and sends an acknowledgment email.

------------------------------------------------------------------------
# Phase 2 --- Supabase data model and Quarkus backend foundation

**Status: Complete (2026-11-17).** The Quarkus backend foundation is in `Backend`; its restricted read-only connection to live Supabase Postgres and health/readiness checks were verified. A backend-only GitHub Actions CI pipeline is in place, with successful checks on `backend_dev` and pull requests. Applicant APIs, HR authentication, dashboard work, deployment, and routing n8n writes through the backend remain future phases.

**Goal:** Prepare a secure Quarkus foundation that reads the data created in Phase 1. Keep Supabase Postgres as the single source of truth. Do not create a parallel MySQL database, copy applicant records, or move the live n8n writes yet.

### Phase 2 step-by-step

1. **Document the current Phase 1 data contract.** Record existing columns, unique Gmail message ID, status/extraction states, JSON fields, and whether each value comes from email or CV extraction. Do not put applicant values in documentation. The data contract is in `Docs/Phase_2_Data_Contract.md`.
2. **Keep the Phase 2 data model bounded.** Use the existing `public.job_applications` table without restructuring it. Design applicant, application, and job relationships with the Phase 3 API contract; any future schema migration must be reviewed separately.
3. **Choose and document the access model.** Completed: Quarkus uses a dedicated, least-privilege role, environment-provided secrets, required table grants, and a role-scoped RLS SELECT policy. Do not use the database owner or privileged service role as the routine application credential.
4. **Clean the generated Quarkus project.** Preserve the Java 25 / Quarkus 3.40.1.2 Maven BOM and plugin. Keep the intended Java extensions: REST Jackson, Hibernate ORM with Panache (repository pattern), PostgreSQL JDBC, and SmallRye Health. Remove demo resources/entities and redundant or Kotlin-specific extensions before connecting to Supabase. Keep test libraries test-scoped.
5. **Configure Supabase safely.** Read `QUARKUS_DATASOURCE_JDBC_URL`, `QUARKUS_DATASOURCE_USERNAME`, and `QUARKUS_DATASOURCE_PASSWORD` from the environment. Use SSL and an appropriate direct or Session Pooler connection for the runtime. Do not use the transaction pooler for Hibernate/JPA.
6. **Map the existing table without changing it.** Add the minimum read-only model, repository, and service for a count or limited query. Set Hibernate schema management to `none` and disable seed/import scripts before pointing the application at Supabase. Keep n8n as the only intake writer until Phase 4.
7. **Add health checks and verify a read-only query.** Use SmallRye Health (`/q/health/live`, `/q/health/ready`). Confirm the app starts and reads a small result without logging CV contents or applicant PII.
8. **Document the Phase 3 handoff.** Record Quarkus run steps, extension versions, environment variable names (never values), schema decisions, and planned API operations.

### Phase 2 scope decisions

- The existing `public.job_applications` table is the starting point. Do not create future tables just to complete the foundation.
- The eventual model may include applicants, jobs, applications, resumes, assessments, interviews, email logs, and audit/automation logs. Add them only when their owning phase needs them.
- HR authentication and authorization must be decided before exposing applicant data through an API or dashboard.
- Supabase Auth can be evaluated for HR sign-in; do not create a custom password table.
- Original CV files remain in Gmail. Private Supabase Storage and a database storage reference are later work, before dashboard CV viewing or download.
- Use a clear Java package structure: `model`, `repository`, `service`, `resource` (Quarkus REST/controller layer), `dto`, and `config`. Keep entities separate from API DTOs; resources call services, services call repositories.
- Quarkus schema management and SQL seed scripts must not create, alter, or seed the live Supabase database.

### Phase 2 tasks

- [x] Document the current `public.job_applications` schema and field mapping in `Docs/Phase_2_Data_Contract.md`, without applicant values.
- [x] Document the current table and defer applicant/application/job normalization decisions to Phase 3 API design; no live schema changes are needed for this read-only foundation.
- [x] Provision and verify the least-privilege read-only database role and targeted RLS policy. Reference SQL is kept within `Backend/database/`.
- [x] Generate the Quarkus Maven project in `Backend` with Java 25 and Quarkus 3.40.1.2.
- [x] Clean generated demo code and remove redundant/Kotlin-specific extensions from the Java project.
- [x] Configure PostgreSQL connection values through environment variables and use SSL.
- [x] Add the existing table mapping and narrow read-only repository/service query.
- [x] Disable Hibernate schema changes and all seed/import scripts for the Supabase connection.
- [x] Add SmallRye Health endpoints and safe diagnostic logging.
- [x] Verify the read-only Supabase connection and readiness check.
- [x] Document local setup and Phase 3 handoff.

### Acceptance criteria

- [x] Quarkus starts without creating, updating, dropping, or seeding Supabase schema/data.
- [x] The backend connects using a dedicated, least-privilege account and a matching RLS policy.
- [x] A limited read-only database check succeeds against Supabase.
- [x] Health endpoints work and reveal no credentials or applicant information.
- [x] No applicant data endpoint is exposed in Phase 2; authentication/authorization remains required before Phase 3 APIs expose applicant data.
- [x] Phase 1 n8n intake continues unchanged and remains the only writer.

**Phase 2 deliverable (complete):** A documented Quarkus foundation that securely checks the existing Supabase Postgres connection with a restricted role, avoids schema/data writes, preserves Phase 1 intake, and runs backend-only CI. The next work is Phase 3: design and implement authenticated REST APIs.

Supabase references: [Connecting to Postgres](https://supabase.com/docs/guides/database/connecting-to-postgres), [Postgres roles](https://supabase.com/docs/guides/database/postgres/roles), and [Securing your data](https://supabase.com/docs/guides/database/secure-data). Quarkus references: [Maven tooling](https://quarkus.io/guides/maven-tooling), [Hibernate ORM](https://quarkus.io/guides/hibernate-orm), and [SmallRye Health](https://quarkus.io/guides/smallrye-health).

------------------------------------------------------------------------
# Phase 3 --- Core Quarkus REST APIs

**Goal:** Implement recruitment business operations before connecting
the full automation.

### Applicant APIs

-   [ ] `GET /api/v1/applicants`
-   [ ] `GET /api/v1/applicants/{id}`
-   [ ] `POST /api/v1/applicants`
-   [ ] `PUT /api/v1/applicants/{id}`

### Job APIs

-   [ ] `GET /api/v1/jobs`
-   [ ] `GET /api/v1/jobs/{id}`
-   [ ] `POST /api/v1/jobs`
-   [ ] `PUT /api/v1/jobs/{id}`
-   [ ] `PATCH /api/v1/jobs/{id}/status`

### Application APIs

-   [ ] `GET /api/v1/applications`
-   [ ] `GET /api/v1/applications/{id}`
-   [ ] `POST /api/v1/applications`
-   [ ] `PATCH /api/v1/applications/{id}/status`

### Integration APIs

-   [ ] `POST /api/v1/integrations/applications/email`
-   [ ] `POST /api/v1/integrations/applications/{id}/processing-status`
-   [ ] `GET /api/v1/integrations/applications/pending-follow-up`

### General backend tasks

-   [ ] Create request and response DTOs.
-   [ ] Implement service classes.
-   [ ] Implement Quarkus REST resources (the controller layer).
-   [ ] Add request validation with the Quarkus Hibernate Validator extension.
-   [ ] Add centralized exception handling.
-   [ ] Add pagination and filtering.
-   [ ] Add service and controller tests.
-   [ ] Test APIs using Postman.
-   [ ] Protect integration endpoints with service authentication.

### Acceptance criteria

-   [ ] Applicant records can be created, retrieved, and updated.
-   [ ] Jobs can be created, retrieved, and updated.
-   [ ] Applications can be created and assigned to a job.
-   [ ] Recruitment status can be updated.
-   [ ] Invalid requests return understandable errors.
-   [ ] Integration endpoints are not publicly accessible without
    authentication.

**Phase 3 deliverable:** Tested recruitment REST API.

------------------------------------------------------------------------

# Phase 4 --- Route n8n intake through Quarkus

**Goal:** Replace the direct n8n-to-Postgres write with the secured Quarkus ingestion API, while keeping Gmail intake, CV parsing/extraction, and acknowledgments in n8n. Do this only after the Phase 3 API is authenticated and tested.

### Tasks

-   [ ] Define the JSON payload exchanged between n8n and Quarkus.
-   [ ] Define how an email is matched to a job (job reference, email
    subject convention, or HR review).
-   [ ] Define a private file-storage location.
-   [ ] Store the original CV securely.
-   [ ] Pass file metadata and storage reference to Quarkus.
-   [ ] Extract candidate information when possible.
-   [ ] Call the authenticated application-ingestion endpoint.
-   [ ] Save the applicant, resume, and application records.
-   [ ] Use Gmail message ID and attachment identifiers for idempotency.
-   [ ] Log workflow execution status.
-   [ ] Add retry and manual-review paths.

### Acceptance criteria

-   [ ] A valid test email creates an application through Quarkus in Supabase Postgres.
-   [ ] The original CV is stored privately.
-   [ ] The database stores a storage reference, not a public CV URL.
-   [ ] Reprocessing the same email does not create a duplicate
    application.
-   [ ] Failed processing is visible and recoverable.

**Phase 4 deliverable:** Gmail → n8n → authenticated Quarkus API → Supabase Postgres, with duplicate protection and recoverable failures.

------------------------------------------------------------------------

# Phase 5 --- HR Dashboard

**Goal:** Allow HR to review and manage the records created by the
backend.

### Tasks

-   [ ] Initialize Next.js with TypeScript.
-   [ ] Configure Tailwind CSS.
-   [ ] Create shared layout and navigation.
-   [ ] Create login page.
-   [ ] Connect authentication to Quarkus.
-   [ ] Create dashboard overview.
-   [ ] Create applicants list.
-   [ ] Add search, filters, and pagination.
-   [ ] Create applicant detail page.
-   [ ] Display application and resume information.
-   [ ] Add authorized CV viewing/downloading.
-   [ ] Create job management pages.
-   [ ] Create application status controls.
-   [ ] Create recruitment pipeline view.
-   [ ] Add loading, empty, success, and error states.

### Acceptance criteria

-   [ ] HR can log in.
-   [ ] HR can view applicants received through Gmail.
-   [ ] HR can view applicant and application details.
-   [ ] HR can access CVs only when authorized.
-   [ ] HR can update application status.
-   [ ] Dashboard data comes from Quarkus APIs, not hardcoded sample
    arrays.

**Phase 5 deliverable:** Working HR dashboard connected to the backend.

------------------------------------------------------------------------

# Phase 6 --- Extraction quality and evidence-based AI assessment

**Goal:** Build on Phase 1 extraction: improve parsing reliability and
compare extracted CV evidence with job-specific criteria to assist HR.
Do not repeat the basic intake and extraction work already completed in
Phase 1.

### Extraction quality tasks

- [x] Phase 1 parses supported PDF/DOCX CVs and extracts basic applicant
      details and work experience with LlamaParse and OpenAI GPT-4.1.
- [ ] Review extraction quality on representative, consented test CVs.
- [ ] Decide whether to keep LlamaParse alone or add Apache Tika as a
      fallback; do not add a second parser without a clear need.
- [ ] Handle empty, malformed, encrypted, and unsupported documents.
- [ ] Store extraction status and relevant metadata.
- [ ] Avoid logging full CV contents unnecessarily.

### AI assessment tasks

- [ ] Define job-specific required and preferred criteria.
- [ ] Ask the AI to return evidence snippets for each criterion.
- [ ] Distinguish `evidenced`, `not_found`, and `unclear`.
- [ ] Validate AI output in Quarkus.
- [ ] Save assessment result and model/criteria version.
- [ ] Display assessment and supporting evidence in the dashboard.
- [ ] Provide manual review when extraction or assessment fails.

### Example job criteria

**Java Backend Developer**

Required: - Java - Quarkus - REST API development - SQL / relational
databases - Object-oriented programming

Preferred: - Microservices - Docker - AWS - CI/CD

### Acceptance criteria

- [ ] Supported CVs parse reliably, with extraction failures visible.
- [ ] Assessment is based on the selected job's criteria.
- [ ] Findings include evidence from the CV where available.
- [ ] Missing information is not treated as proof that a candidate
      lacks a skill.
- [ ] AI does not automatically reject candidates.
- [ ] HR can review the original CV and assessment.

**Phase 6 deliverable:** Reliable extraction and evidence-based,
human-reviewed, job-specific AI assistance.

------------------------------------------------------------------------
# Phase 7 --- HR reminders and recruitment workflow

**Goal:** Remind HR about applications that need attention.

### Tasks

-   [ ] Record application received timestamp.
-   [ ] Decide whether the SLA uses calendar hours or business hours.
-   [ ] Set the correct timezone.
-   [ ] Create a scheduled n8n workflow.
-   [ ] Retrieve applications pending HR action.
-   [ ] Send a reminder after 24 hours when eligible.
-   [ ] Escalate after 48 hours when still eligible.
-   [ ] Stop reminders when HR records contact/review.
-   [ ] Log reminder history.
-   [ ] Test weekends, retries, and status changes.

### Acceptance criteria

-   [ ] Eligible applications receive reminders at the configured
    intervals.
-   [ ] Applications already acted upon do not receive unnecessary
    reminders.
-   [ ] Reminder attempts and outcomes are logged.
-   [ ] The workflow handles failures without silently skipping
    applications.

**Phase 7 deliverable:** Automated HR follow-up workflow.

------------------------------------------------------------------------

# Phase 8 --- Testing, security, and deployment

**Goal:** Prepare a reliable portfolio-ready application.

### Testing

-   [ ] Unit-test backend services.
-   [ ] Integration-test database operations.
-   [ ] Test API validation and authorization.
-   [ ] Test Gmail trigger and email sending.
-   [ ] Test PDF and DOCX extraction.
-   [ ] Test AI response validation.
-   [ ] Test duplicate email processing.
-   [ ] Test workflow retry and failure handling.
-   [ ] Test the complete applicant-to-HR journey.

### Security and privacy

-   [ ] Keep credentials in environment variables or a secrets manager.
-   [ ] Use OAuth for Gmail.
-   [ ] Secure HR authentication and authorization.
-   [ ] Secure n8n-to-backend communication.
-   [ ] Keep CV files private.
-   [ ] Validate actual file content and size.
-   [ ] Consider malware scanning before production.
-   [ ] Avoid storing unnecessary personal information.
-   [ ] Define retention and deletion policies for applicant records.
-   [ ] Treat CV text as untrusted AI input.
-   [ ] Keep human review responsible for hiring decisions.
-   [ ] Back up database and file storage.

### Deployment

-   [ ] Create Dockerfiles where appropriate.
-   [ ] Configure Docker Compose for local services.
-   [ ] Document production environment variables.
-   [ ] Choose hosting for frontend, backend, database, n8n, and file
    storage.
-   [ ] Configure HTTPS.
-   [ ] Configure database and file backups.
-   [ ] Add application and workflow monitoring.
-   [ ] Write deployment and recovery instructions.

### Acceptance criteria

-   [ ] Main end-to-end workflow passes tests.
-   [ ] Failures are logged and can be investigated.
-   [ ] Secrets are not included in the repository.
-   [ ] CV files are not publicly exposed.
-   [ ] Deployment and setup instructions are documented.

**Phase 8 deliverable:** Tested and documented RecruitFlow release.

------------------------------------------------------------------------

# Recommended development order at a glance

| Phase | Focus | Main result |
|---|---|---|
| 0 | Project preparation | Repository and development environment ready |
| 1 | Gmail + n8n + AI + Supabase intake | CV validation, AI extraction, database save, applicant acknowledgment (complete) |
| 2 | Data contract + Quarkus foundation | Complete: restricted Supabase connection, health checks, read-only foundation, backend CI |
| 3 | Core REST APIs | Authenticated applicant, job, and application operations |
| 4 | n8n + backend integration | n8n sends application data through Quarkus into the same Supabase database |
| 5 | HR dashboard | HR can review candidates and manage statuses |
| 6 | Extraction quality + AI assessment | Job-specific findings with evidence and human review |
| 7 | HR reminders | Follow-up reminders for applications needing action |
| 8 | Testing, security, deployment | Reliable documented release |

------------------------------------------------------------------------
# Definition of Done --- Every Phase

Before marking a phase complete:

-   [ ] All tasks for the phase are completed or explicitly deferred.
-   [ ] Acceptance criteria have been tested.
-   [ ] No credentials or private applicant data are committed to Git.
-   [ ] Important decisions and setup steps are documented.
-   [ ] Code and workflow changes are committed to Git.
-   [ ] The next phase can use the current phase's deliverables without
    relying on unfinished features.

## First task to begin

Begin Phase 3 by defining the authenticated REST API contract and HR access model. Design the applicant, application, and job relationships for API needs, then implement protected endpoints with DTO validation and authorization. Keep the existing n8n workflow as the intake writer until the secured ingestion API is ready in Phase 4.

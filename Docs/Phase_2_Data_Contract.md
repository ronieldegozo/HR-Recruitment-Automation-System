# RecruitFlow Phase 2 — Current Data Contract

This document records the current Phase 1 Supabase table for the Spring Boot foundation. It describes the schema only; it contains no applicant records or credentials. It is a baseline for Phase 2, not a request to change the live database.

## Current table

`public.job_applications` in the Supabase Postgres project.

| Column | Postgres type | Default or constraint | Meaning and source |
|---|---|---|---|
| `id` | `uuid` | Primary key; `gen_random_uuid()` | Database row identifier. |
| `applicant_email` | `text` | Required by current intake | Sender address from Gmail. |
| `applicant_name` | `text` | Nullable | Sender display name from Gmail, when present. |
| `gmail_message_id` | `text` | Unique | Gmail message identifier; protects against duplicate application rows. |
| `gmail_thread_id` | `text` | Nullable | Gmail conversation identifier. |
| `email_subject` | `text` | Nullable | Subject of the incoming message. |
| `email_received_at` | `timestamptz` | Nullable | Email received time. |
| `resume_files` | `jsonb` | Default `[]` | Attachment metadata, not the PDF/DOCX file itself. The original CV remains in Gmail. |
| `status` | `text` | Default `received` | Recruitment status at intake. |
| `created_at` | `timestamptz` | Default `now()` | Time Postgres created the row. |
| `extracted_full_name` | `text` | Nullable | Applicant name extracted from the CV by AI. |
| `applicant_mobile_number` | `text` | Nullable | Mobile number extracted from the CV by AI. |
| `home_address` | `text` | Nullable | Address extracted from the CV by AI. |
| `work_experience` | `jsonb` | Default `[]` | Structured work-history entries extracted from the CV. |
| `extraction_status` | `text` | Default `pending`; check constraint allows `pending`, `completed`, `needs_review`, `failed` | Outcome of CV extraction. |
| `extracted_at` | `timestamptz` | Nullable | Time the extraction result was recorded. |

## Important distinctions

- `applicant_name` comes from the Gmail sender display name. `extracted_full_name` comes from the CV. They may differ or one may be empty.
- One row currently represents one email application. The unique `gmail_message_id` is the idempotency key.
- There is no job ID in this table yet. The current intake does not reliably associate an email with a specific job.
- `resume_files` records attachment metadata. It does not contain a private Supabase Storage reference or the actual CV file.
- AI extraction can be incomplete or inaccurate. `extraction_status` records process state; it does not represent a hiring decision.

## Future model mapping (proposal, not implemented)

- Applicant identity and reusable contact details may eventually live in `applicants`.
- Each submitted email/application may eventually become an `applications` row linked to an applicant and, when known, a `jobs` row.
- Resume metadata and a private file-storage reference may eventually live in `resumes`.
- The current `public.job_applications` table remains the Phase 2 source. Any normalization needs a reviewed migration and a data-preserving plan in a later phase.

## Phase 2 access state

At the time this contract was prepared, Row Level Security (RLS) is enabled on `public.job_applications` and there are no policies. A dedicated read-only backend role and a role-specific SELECT policy are prepared in `Databases/phase2_backend_readonly_access.sql`; that SQL has **not** been applied. The Spring Boot datasource is configured read-only and Hibernate schema generation is disabled.
-- Phase 2.A organization API schema.
-- Reference migration only: do not execute until the owner reviews and approves it.
-- First create the login role recruitflow_backend_org out of band with LOGIN, NOBYPASSRLS,
-- NOSUPERUSER, NOCREATEDB, NOCREATEROLE, and a strong password stored outside Git.
-- The runtime role must not own this schema and must not inherit service_role or postgres.

CREATE SCHEMA IF NOT EXISTS recruitflow AUTHORIZATION postgres;
REVOKE ALL ON SCHEMA recruitflow FROM PUBLIC, anon, authenticated;
GRANT USAGE ON SCHEMA recruitflow TO recruitflow_backend_org;

CREATE TABLE recruitflow.stores (
    id UUID PRIMARY KEY,
    manager_user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE RESTRICT,
    store_code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    location VARCHAR(300),
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX stores_manager_status_idx
    ON recruitflow.stores (manager_user_id, status, name);

CREATE TABLE recruitflow.store_team_lead_assignments (
    id UUID PRIMARY KEY,
    store_id UUID NOT NULL REFERENCES recruitflow.stores(id) ON DELETE RESTRICT,
    team_lead_user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE RESTRICT,
    assigned_by_user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE RESTRICT,
    assigned_at TIMESTAMPTZ NOT NULL,
    unassigned_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX store_team_lead_active_unique
    ON recruitflow.store_team_lead_assignments (store_id, team_lead_user_id)
    WHERE unassigned_at IS NULL;
CREATE INDEX store_team_lead_lookup_idx
    ON recruitflow.store_team_lead_assignments (team_lead_user_id, unassigned_at);

CREATE TABLE recruitflow.team_lead_recruiter_assignments (
    id UUID PRIMARY KEY,
    team_lead_assignment_id UUID NOT NULL
        REFERENCES recruitflow.store_team_lead_assignments(id) ON DELETE RESTRICT,
    recruiter_user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE RESTRICT,
    assigned_by_user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE RESTRICT,
    assigned_at TIMESTAMPTZ NOT NULL,
    unassigned_at TIMESTAMPTZ
);

-- A Recruiter has one active reporting line at a time, while assignment history is retained.
CREATE UNIQUE INDEX recruiter_active_assignment_unique
    ON recruitflow.team_lead_recruiter_assignments (recruiter_user_id)
    WHERE unassigned_at IS NULL;
CREATE INDEX recruiter_team_lead_lookup_idx
    ON recruitflow.team_lead_recruiter_assignments (team_lead_assignment_id, unassigned_at);

CREATE TABLE recruitflow.recruiter_progress_reports (
    id UUID PRIMARY KEY,
    assignment_id UUID NOT NULL
        REFERENCES recruitflow.team_lead_recruiter_assignments(id) ON DELETE RESTRICT,
    recruiter_user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE RESTRICT,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    candidates_reviewed INTEGER NOT NULL DEFAULT 0 CHECK (candidates_reviewed >= 0),
    candidates_contacted INTEGER NOT NULL DEFAULT 0 CHECK (candidates_contacted >= 0),
    interviews_scheduled INTEGER NOT NULL DEFAULT 0 CHECK (interviews_scheduled >= 0),
    notes VARCHAR(2000),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT recruiter_progress_period_valid CHECK (period_end >= period_start),
    CONSTRAINT recruiter_progress_assignment_period_unique UNIQUE (assignment_id, period_start)
);

CREATE INDEX recruiter_progress_owner_period_idx
    ON recruitflow.recruiter_progress_reports (recruiter_user_id, period_start DESC);

REVOKE ALL ON ALL TABLES IN SCHEMA recruitflow FROM PUBLIC, anon, authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA recruitflow TO recruitflow_backend_org;

ALTER TABLE recruitflow.stores ENABLE ROW LEVEL SECURITY;
ALTER TABLE recruitflow.stores FORCE ROW LEVEL SECURITY;
CREATE POLICY organization_api_access ON recruitflow.stores
    FOR ALL TO recruitflow_backend_org USING (true) WITH CHECK (true);

ALTER TABLE recruitflow.store_team_lead_assignments ENABLE ROW LEVEL SECURITY;
ALTER TABLE recruitflow.store_team_lead_assignments FORCE ROW LEVEL SECURITY;
CREATE POLICY organization_api_access ON recruitflow.store_team_lead_assignments
    FOR ALL TO recruitflow_backend_org USING (true) WITH CHECK (true);

ALTER TABLE recruitflow.team_lead_recruiter_assignments ENABLE ROW LEVEL SECURITY;
ALTER TABLE recruitflow.team_lead_recruiter_assignments FORCE ROW LEVEL SECURITY;
CREATE POLICY organization_api_access ON recruitflow.team_lead_recruiter_assignments
    FOR ALL TO recruitflow_backend_org USING (true) WITH CHECK (true);

ALTER TABLE recruitflow.recruiter_progress_reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE recruitflow.recruiter_progress_reports FORCE ROW LEVEL SECURITY;
CREATE POLICY organization_api_access ON recruitflow.recruiter_progress_reports
    FOR ALL TO recruitflow_backend_org USING (true) WITH CHECK (true);

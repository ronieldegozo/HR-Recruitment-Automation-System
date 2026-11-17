# Backend CI and production deployment gate

The GitHub Actions workflow `.github/workflows/backend-ci.yml` validates the Quarkus backend on pushes to `backend_dev`, `master`, and `main`, and on pull requests targeting `master` or `main`.

For each run it:

1. Builds and tests with the Maven wrapper and Java 25 (`mvnw verify`).
2. Builds the production Docker image from `Backend/Dockerfile` and the `Backend/` build context without publishing it.

This catches Java build/test failures and Docker packaging failures before a change reaches the live service. The workflow does not need database or Supabase secrets because CI does not connect to production services.

## Require CI before merging

In GitHub, open **Settings → Branches** (or **Rules → Rulesets**) and protect the production branch (`master`, or `main` if that is the repository's production branch). Require pull requests and require the status check **Backend CI (Maven + Docker)** before merging. Keep the production branch as the Render service's deploy branch. This makes the pull request check block a merge when the CI job fails.

The workflow also runs again on the production branch after a merge. Keep the CI workflow enabled and avoid path filters on this workflow so GitHub always reports a check for production commits.

## Make Render wait for CI

In the Render service settings, set **Auto-Deploy** to **After CI Checks Pass**. With this setting, Render waits for GitHub CI checks on the linked production commit and does not start an automatic deploy when a check fails. Confirm the service is linked to the expected production branch and that its health check path is `/q/health/ready`.

GitHub branch protection prevents a failed check from being merged through the normal PR flow; Render's CI gate prevents automatic deployment for a failed production-branch check. Direct pushes that bypass branch protection still reach the production branch, so restrict who can push to it.

## Coverage limits

This pipeline verifies compilation, tests, and Docker image construction. It does not verify production credentials, live Supabase connectivity, or application behavior against real applicant data. Render's readiness health check verifies required runtime dependencies during deploy. Keep secrets in Render's environment settings and never add them to CI logs or repository files.

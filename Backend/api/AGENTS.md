# API collection maintenance rules

These instructions apply to all API work in `Backend/`. Keep API-specific changes inside this folder and follow `../AGENTS.md`.

## Keep Postman in sync with backend APIs

Whenever an API endpoint is created, changed, or removed under `../src/main/java/`:

1. Update `HR Recruiter WF.postman_collection.json` in the same change.
2. Keep matching requests under both top-level folders: `Local` and `Live`. The request name, HTTP method, route, query parameters, headers, auth, body shape, and expected usage must match between them; only the base URL and access-token variables should differ.
3. Use collection/environment variables for URLs, identifiers, payload inputs, and bearer tokens. Never embed a real password, JWT, API key, database URL/credential, personal applicant data, or other secret in the collection or tracked environment template.
4. For new authenticated API calls, use the matching environment token (`localAccessToken` or `liveAccessToken`). Login requests must capture the returned `accessToken` automatically into that environment variable.
5. Update `README.md` when import/setup steps or environment variables change. Keep destructive operations clearly labeled and do not add example bodies to endpoints that accept no body.
6. Validate the collection and environment JSON after editing. Ensure the Local and Live folders have the same endpoint set and method/path pairs.
7. Do not create new collection endpoints that do not correspond to a backend route. Do not remove a request until the corresponding route is removed or the user asks to remove the request.

## Local and Live use

Use `HR Recruiter WF.postman_environment.json` as a local Postman environment template. Configure credentials in Postman itself, then choose Local or Live in the collection. Login saves separate `localAccessToken` and `liveAccessToken` values. Do not export a populated environment or commit it.

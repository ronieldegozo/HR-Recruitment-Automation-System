# Run the backend locally

This guide starts the Quarkus backend from PowerShell, checks its Supabase Postgres connection, and exercises the Phase 3 authentication and admin APIs. All secrets are entered into the current PowerShell process at runtime; do not save them in this file, `application.properties`, Postman exports, or Git.

## 1. Open PowerShell in `Backend`

```powershell
cd 'C:\Users\pc\Desktop\MyPortfolio\HR Recruitment Automation System\Backend'
```

Change the path if the repository is in a different location.

## 2. Set local connection and Auth configuration

The backend uses the dedicated restricted database role, not the Supabase `postgres` owner account. The configured connection uses the Supabase session pooler with SSL:

- Host: `aws-0-ap-southeast-2.pooler.supabase.com`
- Port: `5432`
- Database: `postgres`
- Database username: `recruitflow_backend_readonly.wnoqqpmbtlljivyxjhkq`
- SSL mode: `require`

The database role password is not stored in the repository. Enter it at a hidden prompt. Then provide the Auth keys. The publishable key is a client key; the service-role key is privileged and must stay server-side.

```powershell
function Set-SessionSecret([string]$Name, [string]$Prompt) {
    $secureValue = Read-Host $Prompt -AsSecureString
    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureValue)
    try {
        [Environment]::SetEnvironmentVariable(
            $Name,
            [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr),
            'Process'
        )
    } finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
        $secureValue.Dispose()
    }
}

$env:SUPABASE_URL = 'https://wnoqqpmbtlljivyxjhkq.supabase.co'
$env:SUPABASE_PUBLISHABLE_KEY = Read-Host 'Supabase publishable key'
Set-SessionSecret 'RECRUITFLOW_DB_PASSWORD' 'Password for recruitflow_backend_readonly'
Set-SessionSecret 'SUPABASE_SERVICE_ROLE_KEY' 'Supabase service-role key (server only)'
```

These variables are available to Quarkus and child processes started from this PowerShell window. Closing the window clears them. Re-enter them in a new window. Never set the database password to the Supabase `postgres` owner password; use the password for the dedicated read-only role.

## 3. Start Quarkus

Run in the same PowerShell window where the variables were set:

```powershell
.\mvnw.cmd quarkus:dev
```

Leave this window open. Press `Ctrl+C` to stop the backend. Dev mode reloads code changes; restart it if it does not reload a change.

## 4. Check health and database readiness

In another PowerShell window, check:

```powershell
Invoke-RestMethod 'http://localhost:8080/q/health/live' | ConvertTo-Json -Depth 5
Invoke-RestMethod 'http://localhost:8080/q/health/ready' | ConvertTo-Json -Depth 5
```

The readiness response should report `UP`, including the PostgreSQL connection checks. If it reports `DOWN`, confirm the restricted-role password, pooler settings, network access, and that Quarkus was started from the window that has the environment variables.

## 5. Test login and admin APIs

Open a second PowerShell window for the API calls. The Quarkus server stays running in the first window. The second window does not need the database password or service-role key; it only needs a fresh Super Admin login. Never paste access tokens or passwords into Git-tracked files. Treat tokens like passwords and do not share them in screenshots or chat.

### Log in

```powershell
$adminEmail = Read-Host 'Super Admin email'
$secureAdminPassword = Read-Host 'Super Admin password' -AsSecureString
$adminPasswordPtr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureAdminPassword)
try {
    $adminPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($adminPasswordPtr)
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($adminPasswordPtr)
    $secureAdminPassword.Dispose()
}
$loginBody = @{ email = $adminEmail; password = $adminPassword } | ConvertTo-Json
$login = Invoke-RestMethod -Method Post `
    -Uri 'http://localhost:8080/api/v1/auth/login' `
    -ContentType 'application/json' `
    -Body $loginBody
$token = $login.accessToken
$headers = @{ Authorization = "Bearer $token" }
$adminPassword = $null
Remove-Variable loginBody, login
```

The login response must have `role` equal to `SUPER_ADMIN` for admin operations to work. The access token is kept in `$token` for this PowerShell session; do not print or commit it.

### List recruiters

```powershell
$recruiters = Invoke-RestMethod -Method Get `
    -Uri 'http://localhost:8080/api/v1/admin/recruiters' `
    -Headers $headers
$recruiters | Format-Table userId, email, role
```

Expected: an array containing only users whose `app_metadata.role` is `RECRUITER`. If none are assigned, the response is an empty array.

### Create a recruiter

```powershell
$newEmail = Read-Host 'New recruiter email'
$newPassword = Read-Host 'Temporary recruiter password' -AsSecureString
$newPasswordPtr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($newPassword)
try {
    $plainNewPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($newPasswordPtr)
    $createBody = @{
        email = $newEmail
        password = $plainNewPassword
        role = 'RECRUITER'
    } | ConvertTo-Json
    $created = Invoke-RestMethod -Method Post `
        -Uri 'http://localhost:8080/api/v1/admin/users' `
        -Headers $headers `
        -ContentType 'application/json' `
        -Body $createBody
    $created | Format-List userId, email, role
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($newPasswordPtr)
    $plainNewPassword = $null
    $newPassword.Dispose()
    Remove-Variable createBody -ErrorAction SilentlyContinue
}
```

### Change a user's role

Allowed roles are `RECRUITER` and `SUPER_ADMIN`. Only assign `SUPER_ADMIN` when full administration access is intended.

```powershell
$userId = Read-Host 'User ID to update'
$roleBody = @{ role = 'RECRUITER' } | ConvertTo-Json
Invoke-RestMethod -Method Put `
    -Uri "http://localhost:8080/api/v1/admin/users/$userId/role" `
    -Headers $headers `
    -ContentType 'application/json' `
    -Body $roleBody
```

### Remove a user's role

This keeps the Auth account but clears its application role. A successful result should show `role` as `null`; the account should then disappear from the recruiter list if it no longer has the `RECRUITER` role.

```powershell
$userId = Read-Host 'User ID whose role should be removed'
Invoke-RestMethod -Method Delete `
    -Uri "http://localhost:8080/api/v1/admin/users/$userId/role" `
    -Headers $headers
```

### Delete a user account

This deletes the Supabase Auth account. Use only when account deletion is intended. There is no request body.

```powershell
$userId = Read-Host 'User ID to permanently delete'
Invoke-RestMethod -Method Delete `
    -Uri "http://localhost:8080/api/v1/admin/users/$userId" `
    -Headers $headers
```

## API summary

| Method | Path | Access | Purpose |
| --- | --- | --- | --- |
| `POST` | `/api/v1/auth/login` | Public login | Sign in with email and password; returns tokens and role |
| `GET` | `/api/v1/admin/recruiters` | `SUPER_ADMIN` | List users with the `RECRUITER` role |
| `POST` | `/api/v1/admin/users` | `SUPER_ADMIN` | Create an Auth user with a role |
| `PUT` | `/api/v1/admin/users/{userId}/role` | `SUPER_ADMIN` | Assign or change a role |
| `DELETE` | `/api/v1/admin/users/{userId}/role` | `SUPER_ADMIN` | Clear the role while keeping the account |
| `DELETE` | `/api/v1/admin/users/{userId}` | `SUPER_ADMIN` | Delete the Auth user account |

For role and account management, send `Authorization: Bearer <Super Admin access token>`. Opening an API URL in a browser sends `GET`; use PowerShell or Postman with the method shown above for `PUT` and `DELETE` calls.

## Credential rotation

Database passwords, a service-role key, and account passwords were shared during setup. Treat the shared values as exposed. Rotate the Supabase database credentials and service-role key in the Supabase Dashboard, update the local environment values, reset any affected Auth account passwords, and issue fresh sessions. Do not copy the previously shared values into this or any other tracked file.

# Run the backend locally

This guide starts the Quarkus backend from PowerShell and checks its health endpoints. The database URL and restricted role username are already configured in `src/main/resources/application.properties`. You only need to provide the role password to the current PowerShell session.

## 1. Open PowerShell in the backend folder

```powershell
cd 'C:\Users\pc\Desktop\MyPortfolio\HR Recruitment Automation System\Backend'
```

If your project is in a different location, change the path to match.

## 2. Set the password for this PowerShell window

Run this block and enter the password for the `recruitflow_backend_readonly` database role when prompted. The input will not be shown as you type.

```powershell
$secureDbPassword = Read-Host 'Supabase backend-role password' -AsSecureString
$ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureDbPassword)
try {
    $env:RECRUITFLOW_DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
    Remove-Variable secureDbPassword
}
```

The variable name must be `RECRUITFLOW_DB_PASSWORD`. Quarkus reads it through this setting in `application.properties`:

```properties
quarkus.datasource.password=${RECRUITFLOW_DB_PASSWORD}
```

The environment variable is available only in this PowerShell window and processes started from it. If you close the window, open a new one and repeat this step. Never put the real password in this file, `application.properties`, or Git.

## 3. Start Quarkus in development mode

In the same PowerShell window where you set the password, run:

```powershell
.\mvnw.cmd quarkus:dev
```

Leave this window open while the backend is running. Press `Ctrl+C` in it to stop the server.

## 4. Check backend health

Open these URLs in a browser:

- Liveness: <http://localhost:8080/q/health/live>
- Readiness, including the Supabase connection: <http://localhost:8080/q/health/ready>

Or query readiness from another PowerShell window:

```powershell
Invoke-RestMethod 'http://localhost:8080/q/health/ready' | ConvertTo-Json -Depth 5
```

A healthy response has `"status": "UP"`. Readiness checks the database connection without returning applicant details.

## If readiness says the database password is missing

The Quarkus process did not receive `RECRUITFLOW_DB_PASSWORD`. Stop Quarkus with `Ctrl+C`, set the password again in the same window, and restart it with `.\mvnw.cmd quarkus:dev`. Also confirm the password belongs to the configured `recruitflow_backend_readonly` role.

Other database connection failures can come from an incorrect role password or unavailable Supabase connection. Check the Quarkus startup output and confirm the current connection settings in Supabase.
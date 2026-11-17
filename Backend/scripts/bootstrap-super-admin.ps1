$ErrorActionPreference = 'Stop'

# One-time local bootstrap helper. Never commit or print the Supabase service_role key.
if ([string]::IsNullOrWhiteSpace($env:SUPABASE_URL)) {
    $env:SUPABASE_URL = 'https://wnoqqpmbtlljivyxjhkq.supabase.co'
}
$adminEmail = Read-Host 'Email of the existing Supabase Auth user to promote'
if ([string]::IsNullOrWhiteSpace($env:SUPABASE_SERVICE_ROLE_KEY)) {
    $secureKey = Read-Host 'Supabase service_role key (server-side only)' -AsSecureString
    $keyPtr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureKey)
    try {
        $secretKey = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($keyPtr)
    } finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($keyPtr)
        Remove-Variable secureKey
    }
} else {
    $secretKey = $env:SUPABASE_SERVICE_ROLE_KEY
}

try {
    $headers = @{ apikey = $secretKey; Authorization = "Bearer $secretKey" }
    $allUsers = @()
    $page = 1
    do {
        $uri = "$($env:SUPABASE_URL.TrimEnd('/'))/auth/v1/admin/users?page=$page&per_page=100"
        $result = Invoke-RestMethod -Method Get -Uri $uri -Headers $headers
        $users = @($result.users)
        $allUsers += $users
        $page++
    } while ($users.Count -eq 100)

    $existingAdmin = $allUsers | Where-Object {
        $_.app_metadata.role -eq 'SUPER_ADMIN'
    } | Select-Object -First 1
    if ($null -ne $existingAdmin) {
        throw 'A Super Admin already exists. Use the protected admin API to manage roles.'
    }

    $user = $allUsers | Where-Object {
        $_.email -and $_.email.Equals($adminEmail, [StringComparison]::OrdinalIgnoreCase)
    } | Select-Object -First 1
    if ($null -eq $user) {
        throw 'Create the initial account first under Supabase Dashboard > Authentication > Users, then rerun this script.'
    }

    $metadata = @{}
    if ($null -ne $user.app_metadata) {
        foreach ($property in $user.app_metadata.PSObject.Properties) {
            $metadata[$property.Name] = $property.Value
        }
    }
    $metadata['role'] = 'SUPER_ADMIN'
    $body = @{ app_metadata = $metadata } | ConvertTo-Json -Depth 10
    $userId = [uri]::EscapeDataString([string]$user.id)
    $uri = "$($env:SUPABASE_URL.TrimEnd('/'))/auth/v1/admin/users/$userId"
    $null = Invoke-RestMethod -Method Put -Uri $uri -Headers $headers -ContentType 'application/json' -Body $body

    Write-Output 'Initial Super Admin role assigned. Sign in again to receive a token with the new role.'
} finally {
    $secretKey = $null
    Remove-Variable secretKey -ErrorAction SilentlyContinue
}
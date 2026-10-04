param(
    [string]$DatabaseUser = 'clarigence_app',
    [ValidateRange(1, 65535)]
    [int]$Port = 8081
)

$ErrorActionPreference = 'Stop'
if (-not (Get-Command mvn.cmd -ErrorAction SilentlyContinue)) {
    throw 'Maven is not on PATH. Open the terminal where mvn works and run this script there.'
}

$listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Any, $Port)
try {
    $listener.Start()
} catch {
    throw "Port $Port is unavailable. Run start-local.ps1 -Port with a free port."
} finally {
    $listener.Stop()
}

# Read locally with masked input. Never place this password in command arguments.
$securePassword = Read-Host "Enter the MySQL password for $DatabaseUser (input is hidden)" -AsSecureString
if ($securePassword.Length -eq 0) {
    $securePassword.Dispose()
    throw 'A MySQL password is required. Use the password set on your local database user.'
}

$variableNames = @('DB_URL', 'DB_USERNAME', 'DB_PASSWORD', 'SERVER_PORT', 'SPRING_PROFILES_ACTIVE',
    'CORS_ALLOWED_ORIGINS', 'SESSION_COOKIE_SECURE')
$previousValues = @{}
foreach ($name in $variableNames) {
    $previousValues[$name] = [Environment]::GetEnvironmentVariable($name, 'Process')
}
$passwordPointer = [IntPtr]::Zero
$runExitCode = 1
Push-Location $PSScriptRoot
try {
    $env:DB_URL = 'jdbc:mysql://localhost:3306/clarigence?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
    $env:DB_USERNAME = $DatabaseUser
    $env:SERVER_PORT = [string]$Port
    [Environment]::SetEnvironmentVariable('SPRING_PROFILES_ACTIVE', $null, 'Process')
    $env:CORS_ALLOWED_ORIGINS = 'http://localhost:8000,http://127.0.0.1:8000'
    $env:SESSION_COOKIE_SECURE = 'false'
    $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
    Write-Host "Starting the local backend on http://localhost:$Port. Keep this window open."
    & mvn.cmd --batch-mode --no-transfer-progress spring-boot:run
    $runExitCode = $LASTEXITCODE
} finally {
    foreach ($name in $variableNames) {
        [Environment]::SetEnvironmentVariable($name, $previousValues[$name], 'Process')
    }
    if ($passwordPointer -ne [IntPtr]::Zero) {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
    }
    $securePassword.Dispose()
    Pop-Location
}
if ($runExitCode -ne 0) {
    Write-Host 'Startup failed. Check the earlier application error above; do not share passwords or debug environment output.'
}
exit $runExitCode

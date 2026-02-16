$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$backendDir = Join-Path $projectRoot "Pet'sHeavenBackend\AniProjectBackend"
$frontendDir = Join-Path $projectRoot "ani-home"

Write-Host "Starting Pets Heaven Project..." -ForegroundColor Green
Write-Host ""

if (-not (Test-Path $backendDir)) {
    throw "Backend directory not found: $backendDir"
}

if (-not (Test-Path $frontendDir)) {
    throw "Frontend directory not found: $frontendDir"
}

# Prefer existing JAVA_HOME, otherwise fall back to common install paths.
$javaCandidates = @(
    "C:\Program Files\Java\jdk-17",
    "C:\Program Files\Java\jdk-21"
)

if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME "bin\java.exe"))) {
    foreach ($candidate in $javaCandidates) {
        if (Test-Path (Join-Path $candidate "bin\java.exe")) {
            $env:JAVA_HOME = $candidate
            break
        }
    }
}

if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME "bin\java.exe"))) {
    throw "JAVA_HOME is not set to a valid JDK installation."
}

$env:Path = "$($env:JAVA_HOME)\bin;$($env:Path)"
Write-Host "Using JAVA_HOME: $($env:JAVA_HOME)" -ForegroundColor Yellow

function Test-Port {
    param([int]$Port)
    try {
        $client = New-Object System.Net.Sockets.TcpClient
        $result = $client.BeginConnect("127.0.0.1", $Port, $null, $null)
        $connected = $result.AsyncWaitHandle.WaitOne(500)
        if ($connected -and $client.Connected) {
            $client.EndConnect($result) | Out-Null
            $client.Close()
            return $true
        }
        $client.Close()
        return $false
    } catch {
        return $false
    }
}

function Wait-Port {
    param(
        [int]$Port,
        [int]$TimeoutSeconds
    )

    $end = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $end) {
        if (Test-Port -Port $Port) { return $true }
        Start-Sleep -Seconds 1
    }
    return $false
}

# Stop stale backend/frontend processes from this project to avoid port and jar-lock conflicts.
$staleJava = Get-CimInstance Win32_Process -Filter "name='java.exe'" -ErrorAction SilentlyContinue |
    Where-Object { $_.CommandLine -like "*AniHome-api-0.0.1-SNAPSHOT.jar*" }
foreach ($proc in $staleJava) {
    try { Stop-Process -Id $proc.ProcessId -Force -ErrorAction Stop } catch {}
}

$staleNode = Get-CimInstance Win32_Process -Filter "name='node.exe'" -ErrorAction SilentlyContinue |
    Where-Object { $_.CommandLine -like "*ani-home*" -and $_.CommandLine -like "*react-scripts*" }
foreach ($proc in $staleNode) {
    try { Stop-Process -Id $proc.ProcessId -Force -ErrorAction Stop } catch {}
}

Start-Sleep -Seconds 2

Write-Host "Starting Backend (Spring Boot)..." -ForegroundColor Yellow
$backendCmd = "cd /d `"$backendDir`" && mvnw.cmd -Djava.version=17 -DskipTests package && java -jar target\AniHome-api-0.0.1-SNAPSHOT.jar"
Start-Process cmd -ArgumentList "/k", $backendCmd -WindowStyle Normal

Write-Host "Waiting for backend on port 8080 (up to 300s)..." -ForegroundColor Yellow
$backendReady = Wait-Port -Port 8080 -TimeoutSeconds 300
if (-not $backendReady) {
    Write-Host "Backend did not become reachable on http://localhost:8080." -ForegroundColor Red
    Write-Host "Check the Backend terminal window for errors." -ForegroundColor Red
}

Write-Host "Starting Frontend (React)..." -ForegroundColor Yellow
$frontendCmd = "cd /d `"$frontendDir`" && (if not exist node_modules (npm install)) && npm start"
Start-Process cmd -ArgumentList "/k", $frontendCmd -WindowStyle Normal

Write-Host "Waiting for frontend on port 3000 (up to 240s)..." -ForegroundColor Yellow
$frontendReady = Wait-Port -Port 3000 -TimeoutSeconds 240
if (-not $frontendReady) {
    Write-Host "Frontend did not become reachable on http://localhost:3000." -ForegroundColor Red
    Write-Host "Check the Frontend terminal window for errors." -ForegroundColor Red
}

Write-Host ""
if ($backendReady -and $frontendReady) {
    Write-Host "Both services are running." -ForegroundColor Green
} else {
    Write-Host "One or more services failed to start." -ForegroundColor Red
}
Write-Host "Backend:  http://localhost:8080" -ForegroundColor Cyan
Write-Host "Frontend: http://localhost:3000" -ForegroundColor Cyan

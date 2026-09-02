param(
    [switch]$NoBrowser
)

$ErrorActionPreference = 'Stop'

$root = $PSScriptRoot
$backendDir = Join-Path $root 'StudyFlow-backend'
$frontendDir = Join-Path $root 'StudyFlow-frontend\frontend'
$runtimeDir = Join-Path $root 'runtime'
$backendJar = Join-Path $backendDir 'target\studyflow-backend-0.1.0-SNAPSHOT.jar'
$socketTemp = Join-Path $env:USERPROFILE '.codex\tmp'
$backendLog = Join-Path $runtimeDir 'backend.log'
$frontendLog = Join-Path $runtimeDir 'frontend.log'
$backendPidFile = Join-Path $runtimeDir 'backend.pid'
$frontendPidFile = Join-Path $runtimeDir 'frontend.pid'

function Write-Step($message) {
    Write-Host "[StudyFlow] $message"
}

function Find-CommandPath($name) {
    $command = Get-Command $name -ErrorAction SilentlyContinue
    if ($command) {
        return $command.Source
    }
    return $null
}

function Test-PortOpen($port) {
    $connection = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    return $null -ne $connection
}

function Wait-HttpOk($url, $seconds) {
    $deadline = (Get-Date).AddSeconds($seconds)
    while ((Get-Date) -lt $deadline) {
        try {
            $response = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 2
            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 500) {
                return $true
            }
        } catch {
            Start-Sleep -Milliseconds 800
        }
    }
    return $false
}

New-Item -ItemType Directory -Force -Path $runtimeDir | Out-Null

if (-not (Test-Path $backendJar)) {
    throw "Backend JAR not found: $backendJar. Run mvn package in StudyFlow-backend first."
}

if (-not (Test-Path $socketTemp)) {
    New-Item -ItemType Directory -Force -Path $socketTemp | Out-Null
}

$javaPath = if (Test-Path 'E:\jdk17\bin\java.exe') {
    'E:\jdk17\bin\java.exe'
} else {
    Find-CommandPath 'java.exe'
}

if (-not $javaPath) {
    throw 'java.exe was not found. Install JDK or add java.exe to PATH.'
}

if (-not (Find-CommandPath 'npm.cmd')) {
    throw 'npm.cmd was not found. Install Node.js or add npm to PATH.'
}

if (-not (Test-Path (Join-Path $frontendDir 'node_modules'))) {
    Write-Step 'Frontend dependencies missing. Running npm ci.'
    Push-Location $frontendDir
    try {
        npm ci
    } finally {
        Pop-Location
    }
}

if (Test-PortOpen 8080) {
    Write-Step 'Backend port 8080 is already running. Skip.'
} else {
    Write-Step 'Starting backend: http://localhost:8080'
    $backendArgs = @("-Djdk.net.unixdomain.tmpdir=$socketTemp", '-jar', $backendJar)
    $backendProcess = Start-Process `
        -FilePath $javaPath `
        -ArgumentList $backendArgs `
        -WorkingDirectory $backendDir `
        -RedirectStandardOutput $backendLog `
        -RedirectStandardError (Join-Path $runtimeDir 'backend.err.log') `
        -WindowStyle Hidden `
        -PassThru
    Set-Content -Encoding ASCII -Path $backendPidFile -Value $backendProcess.Id
}

if (Test-PortOpen 5173) {
    Write-Step 'Frontend port 5173 is already running. Skip.'
} else {
    Write-Step 'Starting frontend: http://localhost:5173'
    $frontendProcess = Start-Process `
        -FilePath 'cmd.exe' `
        -ArgumentList @('/c', 'npm run dev -- --host localhost') `
        -WorkingDirectory $frontendDir `
        -RedirectStandardOutput $frontendLog `
        -RedirectStandardError (Join-Path $runtimeDir 'frontend.err.log') `
        -WindowStyle Hidden `
        -PassThru
    Set-Content -Encoding ASCII -Path $frontendPidFile -Value $frontendProcess.Id
}

$backendReady = Wait-HttpOk 'http://localhost:8080/actuator/health' 30
$frontendReady = Wait-HttpOk 'http://localhost:5173/' 30

Write-Host ''
if ($backendReady -and $frontendReady) {
    Write-Step 'Ready.'
    Write-Host 'Frontend: http://localhost:5173/'
    Write-Host 'Backend health: http://localhost:8080/actuator/health'
    if (-not $NoBrowser) {
        Start-Process 'http://localhost:5173/'
    }
} else {
    Write-Step 'Not fully ready. Check logs in runtime/.'
    Write-Host "Backend log: $backendLog"
    Write-Host "Frontend log: $frontendLog"
}

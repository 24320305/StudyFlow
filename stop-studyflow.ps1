$ErrorActionPreference = 'Continue'

$root = $PSScriptRoot
$runtimeDir = Join-Path $root 'runtime'
$pidFiles = @(
    Join-Path $runtimeDir 'frontend.pid',
    Join-Path $runtimeDir 'backend.pid'
)

function Stop-PidTree($processId) {
    if (-not $processId) {
        return
    }
    $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
    if ($process) {
        Write-Host "[StudyFlow] Stopping process $processId"
        taskkill /PID $processId /T /F | Out-Null
    }
}

foreach ($file in $pidFiles) {
    if (Test-Path $file) {
        $pidValue = Get-Content $file -ErrorAction SilentlyContinue | Select-Object -First 1
        Stop-PidTree $pidValue
        Remove-Item $file -Force -ErrorAction SilentlyContinue
    }
}

foreach ($port in @(5173, 8080)) {
    $connections = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    foreach ($connection in $connections) {
        Stop-PidTree $connection.OwningProcess
    }
}

Write-Host '[StudyFlow] Stop command finished.'

$projectRoot = $PSScriptRoot
$java = 'E:\jdk17\bin\java.exe'
$jar = Join-Path $projectRoot 'target\studyflow-backend-0.1.0-SNAPSHOT.jar'
$socketTemp = Join-Path $env:USERPROFILE '.codex\tmp'

if (-not (Test-Path $java)) {
    throw 'Java 17 was not found at E:\jdk17\bin\java.exe.'
}

if (-not (Test-Path $jar)) {
    throw 'The backend JAR was not found. Build the project from the IntelliJ Maven tool window first.'
}

if (-not (Test-Path $socketTemp)) {
    throw 'The Codex temporary directory was not found. Create .codex\tmp under your user profile, then run again.'
}

& $java "-Djdk.net.unixdomain.tmpdir=$socketTemp" -jar $jar

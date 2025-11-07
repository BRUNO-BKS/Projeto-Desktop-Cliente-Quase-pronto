# Rebuild fat JAR and copy to module root
$ErrorActionPreference = 'Stop'

# Resolve paths
$moduleRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$mvn = Resolve-Path (Join-Path $moduleRoot "..\tools\maven\apache-maven-3.9.9\bin\mvn.cmd")

Write-Host "Using Maven: $mvn"

# Run Maven package
& $mvn -f (Join-Path $moduleRoot 'pom.xml') -U -B -DskipTests package

# Copy shaded JAR to module root with a friendly name
$artifact = Join-Path $moduleRoot "target\buyo-adminfx-1.0.0-shaded.jar"
$dest = Join-Path $moduleRoot "buyo-adminfx-1.0.0-shaded.jar"

if (-not (Test-Path $artifact)) {
  throw "Shaded artifact not found: $artifact"
}

Copy-Item -Path $artifact -Destination $dest -Force

Write-Host "Built and copied: $dest"

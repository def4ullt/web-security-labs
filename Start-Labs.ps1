$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
mvn spring-boot:run
if ($LASTEXITCODE -ne 0) { throw 'Application startup failed.' }

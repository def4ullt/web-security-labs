$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
mvn test
if ($LASTEXITCODE -ne 0) { throw 'Tests failed. See target/surefire-reports.' }

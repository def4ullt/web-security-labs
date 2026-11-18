# Run after starting the app. Executes the same sequence as the Postman collection.
$ErrorActionPreference = 'Stop'
$collection = Get-Content (Join-Path $PSScriptRoot 'requests\BookShelf.postman_collection.json') -Raw | ConvertFrom-Json
$vars = @{}
foreach ($v in $collection.variable) { $vars[$v.key] = [string]$v.value }
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
function Expand-LabValue([string]$text) {
    foreach ($key in @($vars.Keys)) { $text = $text.Replace("{{$key}}", $vars[$key]) }
    return $text
}
$results = @()
foreach ($folder in $collection.item) {
    foreach ($item in $folder.item) {
        $r = $item.request
        $headers = @{}
        foreach ($h in $r.header) { $headers[$h.key] = Expand-LabValue $h.value }
        if ($r.auth.type -eq 'basic') {
            $u = Expand-LabValue $r.auth.basic[0].value
            $p = Expand-LabValue $r.auth.basic[1].value
            $headers.Authorization = 'Basic ' + [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("${u}:$p"))
        } elseif ($r.auth.type -eq 'bearer') {
            $headers.Authorization = 'Bearer ' + (Expand-LabValue $r.auth.bearer[0].value)
        }
        $params = @{ Uri=(Expand-LabValue $r.url); Method=$r.method; Headers=$headers; WebSession=$session; SkipHttpErrorCheck=$true }
        if ($r.body) { $params.Body = Expand-LabValue $r.body.raw }
        $response = Invoke-WebRequest @params
        $expected = [int]([regex]::Match($item.name, '\[(\d+)\]').Groups[1].Value)
        $actual = [int]$response.StatusCode
        $results += [pscustomobject]@{Folder=$folder.name; Request=$item.name; Expected=$expected; Actual=$actual; Passed=($expected -eq $actual)}
        Write-Host "$actual $($item.name)"
        if ($actual -ne $expected) { throw "Unexpected response: $($item.name): $($response.Content)" }
        if ($response.Content) {
            $data = $response.Content | ConvertFrom-Json
            if ($r.url -eq '{{base}}/basic/csrf') { $vars.csrf = $data.token }
            if ($r.method -eq 'POST' -and $r.url -match '/books$') { $vars.bookId = [string]$data.id }
            if ($r.url -eq '{{base}}/auth/token') { $login = $r.body.raw | ConvertFrom-Json; $vars["$($login.username)Token"] = $data.access_token }
            if ($r.url -eq '{{base}}/api/audit' -and $actual -eq 200 -and $data.Count -gt 0) { $vars.auditId = [string]$data[0].id }
        }
    }
}
$results | Export-Csv (Join-Path $PSScriptRoot 'docs\request-verification.csv') -NoTypeInformation -Encoding utf8
Write-Host "PASS: $($results.Count) requests. No tokens written to report."

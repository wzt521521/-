$ErrorActionPreference = 'Stop'

$serverLink = Join-Path $env:LOCALAPPDATA 'Microsoft\WinGet\Links\redis-server.exe'
if (-not (Test-Path -LiteralPath $serverLink)) {
    throw 'Redis is not installed.'
}
$serverPath = (Get-Item -LiteralPath $serverLink).Target[0]
$cliPath = Join-Path (Split-Path $serverPath) 'redis-cli.exe'

& $cliPath -h 127.0.0.1 -p 6379 SHUTDOWN SAVE
if ($LASTEXITCODE -ne 0) {
    throw 'Redis did not shut down cleanly.'
}
Write-Output 'Redis stopped after saving persistent data.'

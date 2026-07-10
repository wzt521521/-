$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path $PSScriptRoot -Parent
$workspaceRoot = Split-Path $projectRoot -Parent
$runtimeDir = Join-Path $workspaceRoot '.runtime\redis'
$configPath = Join-Path $projectRoot 'redis\redis.windows.conf'
$serverLink = Join-Path $env:LOCALAPPDATA 'Microsoft\WinGet\Links\redis-server.exe'

if (Get-NetTCPConnection -LocalPort 6379 -State Listen -ErrorAction SilentlyContinue) {
    Write-Output 'Redis is already listening on 127.0.0.1:6379.'
    exit 0
}
if (-not (Test-Path -LiteralPath $serverLink)) {
    throw 'Redis is not installed. Run: winget install --id taizod1024.redis-windows-fork --exact --scope user'
}

$serverPath = (Get-Item -LiteralPath $serverLink).Target[0]
$resolvedConfig = (Resolve-Path -LiteralPath $configPath).Path.Replace('\', '/')
$drive = $resolvedConfig.Substring(0, 1).ToLowerInvariant()
$msysConfig = "/cygdrive/$drive" + $resolvedConfig.Substring(2)

New-Item -ItemType Directory -Force -Path $runtimeDir | Out-Null
$process = Start-Process -FilePath $serverPath -ArgumentList $msysConfig `
    -WorkingDirectory $runtimeDir -WindowStyle Hidden -PassThru

for ($attempt = 0; $attempt -lt 20; $attempt++) {
    $client = [Net.Sockets.TcpClient]::new()
    try {
        $connected = $client.ConnectAsync('127.0.0.1', 6379).Wait(500) -and $client.Connected
    } finally {
        $client.Dispose()
    }
    if ($connected) {
        Write-Output "Redis started (PID $($process.Id)) on 127.0.0.1:6379."
        exit 0
    }
    Start-Sleep -Milliseconds 500
}

throw "Redis failed to start. Check $(Join-Path $runtimeDir 'redis.log')."

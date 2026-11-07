$ErrorActionPreference = 'SilentlyContinue'
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$Runtime = Join-Path $Root '.runtime'
foreach ($name in @('tunnel.pid', 'backend.pid')) {
  $file = Join-Path $Runtime $name
  if (Test-Path $file) {
    $savedPid = Get-Content $file | Select-Object -First 1
    if ($savedPid -match '^\d+$') {
      $proc = Get-Process -Id ([int]$savedPid)
      if ($proc) { Stop-Process -Id ([int]$savedPid) }
    }
    Remove-Item $file -Force
  }
}
Write-Host '兔南电影购票系统已停止。' -ForegroundColor Yellow

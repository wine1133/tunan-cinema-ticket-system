<#
  兔南电影购票系统 - 一键启动
  默认会启动 MySQL、后端、前端静态站点，并创建 Cloudflare 临时公网地址。
  参数：
    -NoTunnel  只启动本地服务
    -Rebuild   强制重新构建前后端
#>
param(
  [switch]$NoTunnel,
  [switch]$Rebuild
)

$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$Runtime = Join-Path $Root '.runtime'
New-Item -ItemType Directory -Force -Path $Runtime | Out-Null
$Port = 8080
$BackendPidFile = Join-Path $Runtime 'backend.pid'
$TunnelPidFile = Join-Path $Runtime 'tunnel.pid'
$BackendOut = Join-Path $Runtime 'backend.out.log'
$BackendErr = Join-Path $Runtime 'backend.err.log'
$TunnelOut = Join-Path $Runtime 'tunnel.out.log'
$TunnelErr = Join-Path $Runtime 'tunnel.err.log'

function Write-Step([string]$Text) {
  Write-Host ""
  Write-Host "  $Text" -ForegroundColor Cyan
}

function Stop-ProcessFromFile([string]$PidFile) {
  if (Test-Path $PidFile) {
    $savedPid = Get-Content $PidFile -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($savedPid -match '^\d+$') {
      $proc = Get-Process -Id ([int]$savedPid) -ErrorAction SilentlyContinue
      if ($proc) { Stop-Process -Id ([int]$savedPid); Start-Sleep -Milliseconds 500 }
    }
  }
}

function Resolve-Java {
  $candidates = @(
    $env:JAVA_HOME,
    'D:\java\jdk-17',
    'C:\Program Files\Java\jdk-17'
  ) | Where-Object { $_ }
  foreach ($javaHome in $candidates) {
    $java = Join-Path $javaHome 'bin\java.exe'
    if (Test-Path $java) { return $java }
  }
  $command = Get-Command java -ErrorAction SilentlyContinue
  if ($command) { return $command.Source }
  throw '未找到 Java 17，请安装 JDK 17 或设置 JAVA_HOME。'
}

function Resolve-Mysql {
  $command = Get-Command mysql -ErrorAction SilentlyContinue
  if ($command) { return $command.Source }
  $candidates = @(
    'D:\phpstudy_pro\Extensions\MySQL8.0.12\bin\mysql.exe',
    'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe',
    'C:\xampp\mysql\bin\mysql.exe'
  )
  foreach ($path in $candidates) { if (Test-Path $path) { return $path } }
  return $null
}

Write-Host ""
Write-Host "  ████████╗██╗   ██╗███╗   ██╗ █████╗ ███╗   ██╗" -ForegroundColor DarkYellow
Write-Host "     ██╔══╝██║   ██║████╗  ██║██╔══██╗████╗  ██║" -ForegroundColor DarkYellow
Write-Host "     ██║   ██║   ██║██╔██╗ ██║███████║██╔██╗ ██║" -ForegroundColor DarkYellow
Write-Host "     ██║   ██║   ██║██║╚██╗██║██╔══██║██║╚██╗██║" -ForegroundColor DarkYellow
Write-Host "     ██║   ╚██████╔╝██║ ╚████║██║  ██║██║ ╚████║" -ForegroundColor DarkYellow
Write-Host "     ╚═╝    ╚═════╝ ╚═╝  ╚═══╝╚═╝  ╚═╝╚═╝  ╚═══╝" -ForegroundColor DarkYellow
Write-Host "              兔南电影购票系统 · 一键启动" -ForegroundColor White
Write-Host ""

Write-Step '1/5 检查 MySQL 数据库'
$mysql = Resolve-Mysql
if ($mysql) {
  $mysqlService = Get-Service | Where-Object { $_.DisplayName -match 'MySQL' -and $_.Status -eq 'Running' } | Select-Object -First 1
  if (-not $mysqlService) {
    $candidate = Get-Service | Where-Object { $_.Name -match '^MySQL' } | Select-Object -First 1
    if ($candidate) {
      try { Start-Service -Name $candidate.Name; Start-Sleep -Seconds 2 } catch { Write-Host '  ! 无法自动启动 MySQL 服务，请手动启动。' -ForegroundColor Yellow }
    }
  }
  $dbUser = if ($env:DB_USER) { $env:DB_USER } else { 'root' }
  $env:MYSQL_PWD = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { 'root' }
  & $mysql "-u$dbUser" -e "CREATE DATABASE IF NOT EXISTS tunan_cinema CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>$null
  Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
  if ($LASTEXITCODE -eq 0) { Write-Host '  ✓ tunan_cinema 数据库已就绪' -ForegroundColor Green }
  else { Write-Host '  ! 数据库连接失败，请检查 DB_USER / DB_PASSWORD 配置。' -ForegroundColor Yellow }
} else {
  Write-Host '  ! 未找到 MySQL 客户端，请先安装 MySQL 8 或使用 Docker Compose。' -ForegroundColor Yellow
}

Write-Step '2/5 构建 Vue 3 前端'
Push-Location (Join-Path $Root 'frontend')
try {
  if ($Rebuild -or -not (Test-Path 'node_modules')) { npm install --no-audit --no-fund }
  npm run build:server
  if ($LASTEXITCODE -ne 0) { throw '前端构建失败' }
  Write-Host '  ✓ 前端已打包进 Spring Boot' -ForegroundColor Green
} finally { Pop-Location }

Write-Step '3/5 构建 Spring Boot 后端'
Stop-ProcessFromFile $BackendPidFile
Push-Location (Join-Path $Root 'backend')
try {
  $env:JAVA_HOME = Split-Path -Parent (Split-Path -Parent (Resolve-Java))
  $env:Path = (Join-Path $env:JAVA_HOME 'bin') + ';' + $env:Path
  mvn -q -DskipTests package
  if ($LASTEXITCODE -ne 0) { throw '后端构建失败' }
  Write-Host '  ✓ 后端构建完成' -ForegroundColor Green
} finally { Pop-Location }

Write-Step '4/5 启动本地服务'
$java = Resolve-Java
$backend = Start-Process -FilePath $java -ArgumentList @('-jar', 'target\tunan-cinema.jar') -WorkingDirectory (Join-Path $Root 'backend') -RedirectStandardOutput $BackendOut -RedirectStandardError $BackendErr -WindowStyle Hidden -PassThru
Set-Content -Path $BackendPidFile -Value $backend.Id -Encoding ascii
$healthy = $false
for ($i = 0; $i -lt 30; $i++) {
  Start-Sleep -Seconds 1
  try {
    $health = Invoke-RestMethod "http://127.0.0.1:$Port/api/public/health" -TimeoutSec 2
    if ($health.status -eq 'UP') { $healthy = $true; break }
  } catch { }
}
if (-not $healthy) {
  Write-Host '  ✗ 后端启动失败，最近日志：' -ForegroundColor Red
  Get-Content $BackendErr -Tail 30 -ErrorAction SilentlyContinue
  exit 1
}
Write-Host "  ✓ 本地站点已启动：http://127.0.0.1:$Port" -ForegroundColor Green

$publicUrl = ''
if (-not $NoTunnel) {
  Write-Step '5/5 创建公网访问地址'
  $cloudflared = Join-Path $Runtime 'cloudflared.exe'
  if (-not (Test-Path $cloudflared)) {
    Write-Host '  正在下载 Cloudflare Tunnel...' -ForegroundColor Gray
    Invoke-WebRequest 'https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe' -OutFile $cloudflared
  }
  Stop-ProcessFromFile $TunnelPidFile
  $tunnel = Start-Process -FilePath $cloudflared -ArgumentList @('tunnel', '--url', "http://127.0.0.1:$Port", '--no-autoupdate') -RedirectStandardOutput $TunnelOut -RedirectStandardError $TunnelErr -WindowStyle Hidden -PassThru
  Set-Content -Path $TunnelPidFile -Value $tunnel.Id -Encoding ascii
  for ($i = 0; $i -lt 40; $i++) {
    Start-Sleep -Seconds 1
    $content = (Get-Content $TunnelErr -Raw -ErrorAction SilentlyContinue) + (Get-Content $TunnelOut -Raw -ErrorAction SilentlyContinue)
    if ($content -match 'https://([a-z0-9-]+)\.trycloudflare\.com') {
      $publicUrl = $Matches[0]
      break
    }
  }
  if ($publicUrl) { Set-Content -Path (Join-Path $Runtime 'public-url.txt') -Value $publicUrl -Encoding ascii; Write-Host "  ✓ 公网地址：$publicUrl" -ForegroundColor Green }
  else { Write-Host '  ! 公网隧道创建超时，请查看 .runtime/tunnel.err.log。' -ForegroundColor Yellow }
} else {
  Write-Step '5/5 已跳过公网隧道'
}

Write-Host ""
Write-Host "  ┌──────────────────────────────────────────────┐" -ForegroundColor DarkGray
Write-Host "  │  本地地址  http://127.0.0.1:$Port               │" -ForegroundColor White
if ($publicUrl) { Write-Host "  │  公网地址  $publicUrl" -ForegroundColor Green }
Write-Host "  │  影迷账号  tunan / 123456                    │" -ForegroundColor Gray
Write-Host "  │  影院账号  cinema / 123456                   │" -ForegroundColor Gray
Write-Host "  │  管理账号  admin / 123456                    │" -ForegroundColor Gray
Write-Host "  └──────────────────────────────────────────────┘" -ForegroundColor DarkGray
Write-Host ""
Write-Host "  服务已在后台运行。关闭请执行 stop.ps1。" -ForegroundColor DarkGray

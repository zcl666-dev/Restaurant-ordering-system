# sync-static.ps1
# 持续同步 static 目录到构建输出目录（防止 HBuilderX 编译时清空）
# 用法: powershell -ExecutionPolicy Bypass -File sync-static.ps1

param(
    [int]$Interval = 2
)

$projectRoot = $PSScriptRoot
$srcDir = Join-Path $projectRoot "static"

# 可能的构建输出目录
$destDirs = @(
    (Join-Path $projectRoot "unpackage\dist\dev\mp-weixin\static"),
    (Join-Path $projectRoot "unpackage\dist\build\mp-weixin\static")
)

function Sync-StaticFiles {
    foreach ($destDir in $destDirs) {
        $parentDir = Split-Path $destDir -Parent
        if (Test-Path $parentDir) {
            if (-not (Test-Path $destDir)) {
                New-Item -ItemType Directory -Path $destDir -Force | Out-Null
            }
            Copy-Item -Path "$srcDir\*" -Destination $destDir -Recurse -Force
        }
    }
    Write-Host "[$(Get-Date -Format 'HH:mm:ss')] static synced" -ForegroundColor Green
}

Write-Host "Watching static/ and syncing every ${Interval}s ..." -ForegroundColor Yellow
Write-Host "Press Ctrl+C to stop" -ForegroundColor Gray

while ($true) {
    Sync-StaticFiles
    Start-Sleep -Seconds $Interval
}

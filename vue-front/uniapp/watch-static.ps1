param(
    [int]$Interval = 3
)

$srcDir = Join-Path $PSScriptRoot "static"
$baseDir = Join-Path $PSScriptRoot "unpackage\dist"

$targets = @(
    @{ Name = "dev";    WatchDir = Join-Path $baseDir "dev\mp-weixin";    DestDir = Join-Path $baseDir "dev\mp-weixin\static" },
    @{ Name = "build";  WatchDir = Join-Path $baseDir "build\mp-weixin";  DestDir = Join-Path $baseDir "build\mp-weixin\static" }
)

$lastCopyTimes = @{}
foreach ($t in $targets) { $lastCopyTimes[$t.Name] = [DateTime]::MinValue }

Write-Host "=== Static Folder Auto-Copy (dev + build) ===" -ForegroundColor Green
Write-Host "Source: $srcDir" -ForegroundColor Gray
Write-Host "Check every ${Interval}s ..." -ForegroundColor Gray
Write-Host "Press Ctrl+C to stop" -ForegroundColor Gray
Write-Host "============================================" -ForegroundColor Green

$srcExists = Test-Path $srcDir
if (-not $srcExists) {
    Write-Host "[$(Get-Date -Format 'HH:mm:ss')] ERROR: source static folder not found: $srcDir" -ForegroundColor Red
}

while ($true) {
    $srcExists = Test-Path $srcDir

    foreach ($t in $targets) {
        $watchDir = $t.WatchDir
        $destDir  = $t.DestDir
        $name     = $t.Name

        $dirExists = Test-Path $watchDir
        if (-not ($dirExists -and $srcExists)) { continue }

        $appJsonPath = Join-Path $watchDir "app.json"
        if (-not (Test-Path $appJsonPath)) { continue }

        $appJsonTime = (Get-Item $appJsonPath).LastWriteTime
        if ($appJsonTime -gt $lastCopyTimes[$name]) {
            $srcTime = (Get-Item $srcDir).LastWriteTime
            $tabBarPath = Join-Path $destDir "tabBar"

            if ($srcTime -gt $lastCopyTimes[$name] -or -not (Test-Path $tabBarPath)) {
                $dupPath = Join-Path $destDir "static"
                if (Test-Path $dupPath) {
                    Remove-Item $dupPath -Recurse -Force
                    Write-Host "[$(Get-Date -Format 'HH:mm:ss')] [$name] cleaned duplicate static/static" -ForegroundColor Yellow
                }

                if (-not (Test-Path $destDir)) {
                    New-Item -ItemType Directory -Path $destDir -Force | Out-Null
                }

                Get-ChildItem -Path $srcDir | ForEach-Object {
                    Copy-Item -Path $_.FullName -Destination $destDir -Recurse -Force
                }

                $lastCopyTimes[$name] = $appJsonTime
                Write-Host "[$(Get-Date -Format 'HH:mm:ss')] [$name] static folder auto-copied" -ForegroundColor Green
            }
        }
    }

    Start-Sleep -Seconds $Interval
}

@echo off
cd /d "%~dp0"
title Static Auto-Copy (dev + build)

echo.
echo ===========================================
echo   Static Folder Auto-Copy Daemon
echo   Supports dev (dev build) and build (release)
echo   Auto-copies static folder after each compile
echo   Keep this window open
echo ===========================================
echo.

powershell -ExecutionPolicy Bypass -File "%~dp0watch-static.ps1"

pause
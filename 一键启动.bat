@echo off
chcp 65001 >nul
cd /d "%~dp0"
title 兔南电影购票系统
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0start.ps1"
echo.
pause

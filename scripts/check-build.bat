@echo off
REM Double-clickable wrapper for check-build.ps1.
REM
REM Exists so the build check needs no PowerShell knowledge and no execution-policy change:
REM -ExecutionPolicy Bypass applies to this one invocation only and changes nothing on the
REM machine. Pass a scope through, e.g.  check-build.bat full
REM
REM Scopes: quick (default) | full | lint | tests | coverage | all

setlocal
set SCOPE=%1
if "%SCOPE%"=="" set SCOPE=quick

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0check-build.ps1" -Scope %SCOPE%

echo.
pause

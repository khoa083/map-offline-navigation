@echo off
REM Double-clickable wrapper for signing-fingerprint.ps1.
REM
REM   signing-fingerprint.bat              debug keystore
REM   signing-fingerprint.bat release      release keystore (needs release.properties)
REM
REM For reading the certificate out of a built APK, call the .ps1 directly:
REM   powershell -ExecutionPolicy Bypass -File scripts\signing-fingerprint.ps1 -Apk <path.apk>

setlocal
set TARGET=%1
if "%TARGET%"=="" set TARGET=debug

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0signing-fingerprint.ps1" -Target %TARGET%

echo.
pause

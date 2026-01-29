@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo [ContinueProject] Stopping All Services...
echo ========================================================
echo.

echo [1/2] Terminating Java Processes...
taskkill /F /IM java.exe /T 2>nul
if %errorlevel% neq 0 (
    echo Using PowerShell to force kill Java processes...
    powershell -Command "Get-Process -Name java -ErrorAction SilentlyContinue | Stop-Process -Force"
)

echo.
echo [2/2] Terminating Node.js Processes...
taskkill /F /IM node.exe /T 2>nul
if %errorlevel% neq 0 (
    echo Using PowerShell to force kill Node processes...
    powershell -Command "Get-Process -Name node -ErrorAction SilentlyContinue | Stop-Process -Force"
)

echo.
echo ========================================================
echo Service cleanup complete.
echo ========================================================
pause

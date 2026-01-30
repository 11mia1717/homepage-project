@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo [ContinueProject] Stopping All Services...
echo ========================================================
echo.

echo [1/2] Terminating Backend Ports (8085, 8086)...
for %%p in (8085 8086) do (
    for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr :%%p ^| findstr LISTENING') do (
        echo Killing PID %%a on port %%p
        taskkill /F /PID %%a >nul 2>&1
    )
)

echo.
echo [2/2] Terminating Frontend Ports (5175, 5176)...
for %%p in (5175 5176) do (
    for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr :%%p ^| findstr LISTENING') do (
        echo Killing PID %%a on port %%p
        taskkill /F /PID %%a >nul 2>&1
    )
)

echo.
echo ========================================================
echo Service cleanup complete.
echo ========================================================
pause

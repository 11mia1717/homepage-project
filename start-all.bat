@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo [ContinueProject] Starting All Services...
echo ========================================================
echo.

:: 0. Check Environment
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java not found in PATH. Please install Java 21+.
    pause
    exit /b 1
)

:: 1. Backend - Entrusting Client (Continue Bank)
echo [1/4] Starting Entrusting Client Backend (Port 8085)...
if exist "entrusting-client\backend\target\backend-0.0.1-SNAPSHOT.jar" (
    start "Entrusting Client Backend" cmd /k "cd entrusting-client\backend && java -jar target\backend-0.0.1-SNAPSHOT.jar"
) else (
    echo [ERROR] Entrusting Client JAR not found. Please build first.
)
timeout /t 2 >nul

:: 2. Backend - V-PASS (Trustee Provider)
echo [2/4] Starting V-PASS Backend (Port 8086)...
if exist "trustee-provider\backend\target\backend-0.0.1-SNAPSHOT.jar" (
    start "V-PASS Backend" cmd /k "cd trustee-provider\backend && java -jar target\backend-0.0.1-SNAPSHOT.jar"
) else (
    echo [ERROR] V-PASS JAR not found. Please build first.
)
timeout /t 2 >nul

:: 3. Backend - TM Center (Currently Skipped)
echo [SKIP] TM Center Backend code missing (Port 8080 expected).

:: 4. Frontend - Entrusting Client
echo [3/4] Starting Entrusting Client Frontend (Port 5175)...
if exist "entrusting-client\frontend\package.json" (
    start "Entrusting Client Frontend" cmd /k "cd entrusting-client\frontend && npm run dev -- --port 5175 --strictPort"
) else (
    echo [ERROR] Entrusting Client Frontend not found.
)
timeout /t 1 >nul

:: 5. Frontend - V-PASS
echo [4/4] Starting V-PASS Frontend (Port 5176)...
if exist "trustee-provider\frontend\package.json" (
    start "V-PASS Frontend" cmd /k "cd trustee-provider\frontend && npm run dev -- --port 5176 --strictPort"
) else (
    echo [ERROR] V-PASS Frontend not found.
)

echo.
echo ========================================================
echo All services executing in new windows!
echo If a window closes immediately, check the error message.
echo ========================================================
echo.
pause

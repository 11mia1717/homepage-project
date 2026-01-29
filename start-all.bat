@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo [ContinueProject] Starting All Services (MySQL Docker DB)
echo ========================================================
echo.

:: 0. Check Environment
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java not found in PATH. Please install Java 17+.
    pause
    exit /b 1
)

:: Check if Maven is available
where mvn >nul 2>&1
if %errorlevel% equ 0 (
    set MVN_CMD=mvn
) else (
    if exist "entrusting-client\backend\mvnw.cmd" (
        set MVN_CMD=mvnw.cmd
    ) else (
        echo [WARNING] Maven not found. Trying to run existing JAR files...
        goto :run_jars
    )
)

:: Build if Maven available
echo [BUILD] Building backends with Maven...
echo.

echo [1/2] Building Entrusting Client Backend...
cd entrusting-client\backend
call %MVN_CMD% clean package -DskipTests -q
if %errorlevel% neq 0 (
    echo [ERROR] Build failed for Entrusting Client
) else (
    echo [OK] Entrusting Client build successful
)
cd ..\..

echo [2/2] Building SSAP Backend...
cd trustee-provider\backend
call %MVN_CMD% clean package -DskipTests -q
if %errorlevel% neq 0 (
    echo [ERROR] Build failed for SSAP
) else (
    echo [OK] SSAP build successful
)
cd ..\..

echo.
echo [BUILD] Build complete!
echo.

:run_jars
echo ========================================================
echo [RUN] Starting Backend Services...
echo ========================================================
echo.

:: 1. Backend - Entrusting Client (Continue Bank)
echo [1/6] Starting Entrusting Client Backend (Port 8085)...
if exist "entrusting-client\backend\target\backend-0.0.1-SNAPSHOT.jar" (
    start "Entrusting-Backend-8085" cmd /k "cd entrusting-client\backend && java -jar target\backend-0.0.1-SNAPSHOT.jar"
    echo [OK] Entrusting Client Backend starting...
) else (
    echo [ERROR] Entrusting Client JAR not found. Build required.
)
timeout /t 5 >nul

:: 2. Backend - SSAP (Trustee Provider)
echo [2/6] Starting SSAP Backend (Port 8086)...
if exist "trustee-provider\backend\target\backend-0.0.1-SNAPSHOT.jar" (
    start "SSAP-Backend-8086" cmd /k "cd trustee-provider\backend && java -jar target\backend-0.0.1-SNAPSHOT.jar"
    echo [OK] SSAP Backend starting...
) else (
    echo [ERROR] SSAP JAR not found. Build required.
)
timeout /t 5 >nul

:: 3. Backend - TM Center
echo [3/6] Starting TM Center Backend (Port 8082)...
if exist "tm-center\backend\target\backend-0.0.1-SNAPSHOT.jar" (
    start "TM-Center-Backend-8082" cmd /k "cd tm-center\backend && java -jar target\backend-0.0.1-SNAPSHOT.jar"
    echo [OK] TM Center Backend starting...
) else (
    echo [ERROR] TM Center JAR not found.
)
timeout /t 5 >nul

echo.
echo ========================================================
echo [RUN] Starting Frontend Services...
echo ========================================================
echo.

:: 4. Frontend - Entrusting Client
echo [4/6] Starting Entrusting Client Frontend (Port 5175/5178)...
if exist "entrusting-client\frontend\package.json" (
    start "Entrusting-Frontend" cmd /k "cd entrusting-client\frontend && npm run dev"
    echo [OK] Entrusting Client Frontend starting...
) else (
    echo [ERROR] Entrusting Client Frontend not found.
)
timeout /t 2 >nul

:: 5. Frontend - SSAP
echo [5/6] Starting SSAP Frontend (Port 5176)...
if exist "trustee-provider\frontend\package.json" (
    start "SSAP-Frontend" cmd /k "cd trustee-provider\frontend && npm run dev"
    echo [OK] SSAP Frontend starting...
) else (
    echo [ERROR] SSAP Frontend not found.
)
timeout /t 2 >nul

:: 6. Frontend - TM Center
echo [6/6] Starting TM Center Frontend (Port 5178/5177)...
if exist "tm-center\frontend\package.json" (
    start "TM-Center-Frontend" cmd /k "cd tm-center\frontend && npm run dev"
    echo [OK] TM Center Frontend starting...
) else (
    echo [ERROR] TM Center Frontend not found.
)

echo.
echo ========================================================
echo ALL SERVICES STARTED!
echo ========================================================
echo.
echo   [Backend]
echo     - Entrusting Client: http://localhost:8085
echo     - SSAP:              http://localhost:8086
echo     - TM Center:         http://localhost:8082
echo.
echo   [Frontend]
echo     - Continue Bank:     http://localhost:5178 (or 5175)
echo     - SSAP Auth:         http://localhost:5176
echo     - TM Center Web:     http://localhost:5177 (or 5178)
echo.
echo ========================================================
pause

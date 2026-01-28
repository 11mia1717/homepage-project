@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

:: ========================================================
:: [설정] 실행 파일 절대 경로
:: ========================================================
set "MVN_CMD=C:\Program Files\Apache\maven\apache-maven-3.9.12-bin\bin\mvn.cmd"
set "NPM_CMD=C:\Program Files\nodejs\npm.cmd"
set "DOCKER_BIN=C:\Program Files\Docker\Docker\resources\bin"
set "DOCKER_CMD=%DOCKER_BIN%\docker.exe"

:: DB 접속 포트 고정 (3306)
set "DB_PORT=3306"

:: Docker Credential Error 방지를 위한 PATH 임시 추가
set "PATH=%PATH%;%DOCKER_BIN%"

:: ========================================================
:: [초기화] 관리자 권한 확인 및 상승
:: ========================================================
>nul 2>&1 "%SYSTEMROOT%\system32\cacls.exe" "%SYSTEMROOT%\system32\config\system"
if '%errorlevel%' NEQ '0' (
    echo [알림] 관리자 권한을 요청합니다...
    goto UACPrompt
) else ( goto gotAdmin )

:UACPrompt
    echo Set UAC = CreateObject^("Shell.Application"^) > "%temp%\getadmin.vbs"
    echo UAC.ShellExecute "%~s0", "", "", "runas", 1 >> "%temp%\getadmin.vbs"
    "%temp%\getadmin.vbs"
    exit /B

:gotAdmin
    if exist "%temp%\getadmin.vbs" ( del "%temp%\getadmin.vbs" )
    :: 8.3 짧은 경로(CONTIN~1) 호환성 문제 해결을 위해 명시적 경로 이동
    cd /d "C:\ContinueProject\homepage-project"

title [Continue Project] 전체 서비스 통합 실행기 (Final Fixed Path)

echo.
echo ========================================================
echo        [Continue Project] 원클릭 서비스 실행
echo ========================================================
echo.

:: ========================================================
:: [1단계] Docker 실행 상태 점검
:: ========================================================
echo [1/5] Docker 상태 확인 중...
if exist "%DOCKER_CMD%" (
    "%DOCKER_CMD%" info >nul 2>&1
) else (
    docker info >nul 2>&1
)

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [치명적 오류] Docker가 실행되어 있지 않습니다!
    echo --------------------------------------------------------
    echo 1. 윈도우 시작 메뉴에서 'Docker Desktop'을 실행하세요.
    echo 2. 우측 하단 트레이 아이콘이 안정될 때까지 기다리세요.
    echo 3. 그 후 다시 이 파일을 실행해주세요.
    echo --------------------------------------------------------
    pause
    exit /b
)
echo   - Docker 실행 확인됨.

:: ========================================================
:: [2단계] 포트 점유 프로세스 정리 (8085, 8086, 5175, 5177)
:: 주의: DB 포트(3306)는 Docker가 사용 중이므로 죽이지 않음
:: ========================================================
echo.
echo [2/5] 포트 충돌 방지
set PORTS=8085 8086 8088 5175 5176

for %%P in (%PORTS%) do (
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr :%%P ^| findstr LISTENING') do (
        if "%%a" neq "" (
            echo   - 포트 %%P 점유 [PID: %%a] - 강제 종료
            taskkill /F /PID %%a >nul 2>&1
        )
    )
)
echo   - 포트 정리 완료.

:: ========================================================
:: [3단계] 프론트엔드 캐시 정리
:: ========================================================
echo.
echo [3/5] Vite 캐시 정리
if exist "vpass-provider\frontend\node_modules\.vite" (
    rmdir /s /q "vpass-provider\frontend\node_modules\.vite"
)
if exist "entrusting-client\frontend\node_modules\.vite" (
    rmdir /s /q "entrusting-client\frontend\node_modules\.vite"
)
echo   - 캐시 삭제 완료.

:: ========================================================
:: [4단계] DB 컨테이너 실행 및 대기 (Fixed Port 3306)
:: ========================================================
echo.
echo [4/5] MySQL DB 실행 및 대기 (Port: %DB_PORT%)
echo   - DB 컨테이너 상태 확인 중...

:: 컨테이너가 이미 실행 중인지 확인
"%DOCKER_CMD%" ps --filter "name=shared-db" --filter "status=running" --format "{{.Names}}" | findstr "shared-db" >nul
if %errorlevel% equ 0 (
    echo   - [확인] DB가 이미 정상 실행 중입니다. (기존 컨테이너 유지)
) else (
    echo   - DB 컨테이너가 없거나 중지 상태입니다. 실행을 시작합니다...
    if exist "%DOCKER_CMD%" (
        "%DOCKER_CMD%" compose up -d mysql
    ) else (
        docker compose up -d mysql
    )
)

echo.
echo   [대기] DB 상태 확인 및 대기 (10초)...
timeout /t 10 >nul
echo   - DB 준비 완료 가정.

:: ========================================================
:: [5단계] 백엔드 및 프론트엔드 실행
:: ========================================================
echo.
echo [5/5] 애플리케이션 실행
echo   - 새 창에서 서버를 띄웁니다.

if not exist "%MVN_CMD%" (
    echo [오류] Maven 경로 확인 필요: "%MVN_CMD%"
    pause
    exit /b
)

:: 백엔드 실행 (고정 포트 3306 사용)
:: V-PASS Provider (강제 포트 고정: 8086)
set "TRUSTEE_BACKEND_PORT=8086"
start "Backend: V-PASS (8086)" cmd /k "cd /d "C:\ContinueProject\homepage-project\vpass-provider\backend" && set "DB_URL=jdbc:mysql://localhost:%DB_PORT%/trustee_db?useSSL=false^&serverTimezone=UTC^&allowPublicKeyRetrieval=true" && set "DB_USER=root" && set "DB_PASSWORD=password" && set "TRUSTEE_BACKEND_PORT=8086" && call "%MVN_CMD%" spring-boot:run -Dspring-boot.run.arguments=--server.port=8086"

:: Entrusting Client
start "Backend: Client (8085)" cmd /k "cd /d "C:\ContinueProject\homepage-project\entrusting-client\backend" && set "DB_HOST=localhost" && set "DB_PORT=%DB_PORT%" && set "DB_NAME=entrusting_db" && set "DB_USER=root" && set "DB_PASSWORD=password" && call "%MVN_CMD%" spring-boot:run"

echo   - 백엔드 실행 요청됨. 초기화 완료까지 충분히 대기 (30초)...
timeout /t 30 >nul

:: 프론트엔드 실행 (고정 포트 5175, 5176)
if exist "%NPM_CMD%" (
    start "Frontend: V-PASS (5176)" cmd /k "cd /d "C:\ContinueProject\homepage-project\vpass-provider\frontend" && call "%NPM_CMD%" run dev -- --port 5176 --strictPort --force"
    start "Frontend: Client (5175)" cmd /k "cd /d "C:\ContinueProject\homepage-project\entrusting-client\frontend" && call "%NPM_CMD%" run dev -- --port 5175 --strictPort --force"
) else (
    echo   [주의] NPM 절대 경로를 찾지 못해 PATH의 npm을 사용합니다.
    start "Frontend: V-PASS (5176)" cmd /k "cd /d "C:\ContinueProject\homepage-project\vpass-provider\frontend" && npm run dev -- --port 5176 --strictPort --force"
    start "Frontend: Client (5175)" cmd /k "cd /d "C:\ContinueProject\homepage-project\entrusting-client\frontend" && npm run dev -- --port 5175 --strictPort --force"
)

echo.
echo ========================================================
echo  모든 작업이 완료되었습니다!
echo  다음 주소에서 테스트하세요:
echo  1. 위탁사(Client): http://localhost:5175
echo  2. 수탁사(V-PASS): http://localhost:5176
echo ========================================================
pause

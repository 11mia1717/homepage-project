@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

:: 관리자 권한 확인 및 상승
>nul 2>&1 "%SYSTEMROOT%\system32\cacls.exe" "%SYSTEMROOT%\system32\config\system"
if '%errorlevel%' NEQ '0' (
    echo 관리자 권한을 요청합니다...
    goto UACPrompt
) else ( goto gotAdmin )

:UACPrompt
    echo Set UAC = CreateObject^("Shell.Application"^) > "%temp%\getadmin.vbs"
    echo UAC.ShellExecute "%~s0", "", "", "runas", 1 >> "%temp%\getadmin.vbs"
    "%temp%\getadmin.vbs"
    exit /B

:gotAdmin
    if exist "%temp%\getadmin.vbs" ( del "%temp%\getadmin.vbs" )
    pushd "%CD%"
    CD /D "%~dp0"

title DB 초기화 및 실행 (V2 - Docker PATH 수정)

:: Docker 경로 설정
set "DOCKER_BIN=C:\Program Files\Docker\Docker\resources\bin"
set "DOCKER_CMD=%DOCKER_BIN%\docker.exe"

:: PATH에 Docker 경로 임시 추가 (자격 증명 오류 해결)
set "PATH=%PATH%;%DOCKER_BIN%"

echo ========================================================
echo        [Continue Project] DB 수동 실행 도우미
echo ========================================================
echo.
echo 이 스크립트는 Docker DB를 실행하고 이미지를 다운로드합니다.
echo 처음 실행 시 다운로드에 시간이 걸릴 수 있습니다.
echo Docker PATH: %DOCKER_BIN%
echo.

if exist "%DOCKER_CMD%" (
    echo [Docker] DB 실행 명령 전송...
    "%DOCKER_CMD%" compose up -d
) else (
    echo [알림] Docker 절대 경로를 찾을 수 없어 기본 명령어로 시도합니다.
    docker-compose up -d
)

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [오류] Docker 실행 실패.
    echo 1. Docker Desktop이 켜져 있는지 확인해주세요.
    echo 2. 인터넷 연결을 확인해주세요 (이미지 다운로드).
    pause
    exit /b
)

echo.
echo ========================================================
echo [대기] DB 초기화 및 이미지 다운로드 중... (60초 대기)
echo 이 창을 닫지 마세요!
echo ========================================================
timeout /t 60

echo.
echo ========================================================
echo [완료] DB가 준비된 것 같습니다.
echo 이제 'start_all_services.bat' 파일을 실행하여 서비스를 시작하세요!
echo ========================================================
pause

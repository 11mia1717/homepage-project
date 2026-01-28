@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

:: Docker 경로 및 PATH 설정 (자격 증명 오류 방지)
set "DOCKER_BIN=C:\Program Files\Docker\Docker\resources\bin"
set "DOCKER_CMD=%DOCKER_BIN%\docker.exe"
set "PATH=%PATH%;%DOCKER_BIN%"

:: 관리자 권한 상승
>nul 2>&1 "%SYSTEMROOT%\system32\cacls.exe" "%SYSTEMROOT%\system32\config\system"
if '%errorlevel%' NEQ '0' (
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

title Docker 완전 초기화

echo ========================================================
echo        [경고] Docker 완전 초기화 (데이터 삭제)
echo ========================================================
echo.
echo 1. 모든 컨테이너 중지 및 삭제
echo 2. 모든 이미지 삭제
echo 3. 모든 볼륨(DB 데이터) 삭제
echo.
echo 진행하려면 아무 키나 누르세요...
pause >nul

echo.
echo [1/3] 컨테이너 삭제 중...
if exist "%DOCKER_CMD%" (
    for /f "tokens=*" %%i in ('"%DOCKER_CMD%" ps -aq') do "%DOCKER_CMD%" rm -f %%i >nul 2>&1
)

echo [2/3] 이미지 삭제 중...
if exist "%DOCKER_CMD%" (
    for /f "tokens=*" %%i in ('"%DOCKER_CMD%" images -aq') do "%DOCKER_CMD%" rmi -f %%i >nul 2>&1
)

echo [3/3] 볼륨 정리 중...
if exist "%DOCKER_CMD%" (
    "%DOCKER_CMD%" volume prune -f >nul 2>&1
)

echo.
echo ========================================================
echo 초기화 완료.
echo 이제 start_all_services.bat을 실행하세요.
echo ========================================================
pause

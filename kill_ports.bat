@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

:: 관리자 권한 확인
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

echo ========================================================
echo 포트 점유 프로세스 정리 스크립트
echo 대상 포트: 8085, 8086, 5175, 5176, 3306
echo ========================================================

set PORTS=8085 8086 5175 5176 3306

for %%P in (%PORTS%) do (
    echo.
    echo [포트 %%P] 점유 프로세스 확인 중...
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr :%%P ^| findstr LISTENING') do (
        set PID=%%a
        if "!PID!" neq "" (
            echo  - PID !PID! 발견. 종료 시도...
            taskkill /F /PID !PID!
            if !errorlevel! equ 0 (
                echo    성공: 프로세스 종료됨
            ) else (
                echo    실패: 접근 거부되거나 이미 종료됨
            )
        )
    )
)

echo.
echo ========================================================
echo 포트 정리 완료.
echo ========================================================
exit /b

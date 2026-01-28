@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

:: Docker 경로
set "DOCKER_BIN=C:\Program Files\Docker\Docker\resources\bin"
set "DOCKER_CMD=%DOCKER_BIN%\docker.exe"
set "PATH=%PATH%;%DOCKER_BIN%"

echo ========================================================
echo        [DB 상태 진단 도구]
echo ========================================================
echo.

echo [1] Docker 컨테이너 상태 (docker ps -a)
echo --------------------------------------------------------
if exist "%DOCKER_CMD%" (
    "%DOCKER_CMD%" ps -a
) else (
    docker ps -a
)
echo --------------------------------------------------------
echo.

echo [2] DB 컨테이너 로그 (docker logs shared-db)
echo --------------------------------------------------------
if exist "%DOCKER_CMD%" (
    "%DOCKER_CMD%" logs shared-db --tail 20
) else (
    docker logs shared-db --tail 20
)
echo --------------------------------------------------------
echo.

echo [3] 3306 포트 점유 상태 (netstat)
echo --------------------------------------------------------
netstat -ano | findstr :3306
echo --------------------------------------------------------
echo.

echo ========================================================
echo 위 내용을 모두 복사해서 알려주세요!
echo ========================================================
pause

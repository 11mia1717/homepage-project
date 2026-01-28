@echo off
chcp 65001
echo ========================================================
echo 전체 서비스 재시작 스크립트 (CORS 수정 적용)
echo ========================================================

echo.
echo [1/4] 기존 프로세스 정리 중...
taskkill /F /IM java.exe /T 2>nul
taskkill /F /IM node.exe /T 2>nul
echo 완료.

echo.
echo [2/4] 데이터베이스(MySQL) 실행 확인 중...
REM Docker Compose로 DB 실행 시도
docker-compose up -d 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Docker Compose 실행 실패. Docker Desktop이 실행 중인지 확인해주세요.
    echo 만약 로컬 MySQL을 사용 중이라면 이 메시지를 무시하세요.
) else (
    echo Docker DB 실행 시도 완료.
)

echo.
echo [3/4] 백엔드 서비스 시작 중...
echo V-PASS Provider Backend (Port 8086) 시작...
start "V-PASS Backend" cmd /c "cd vpass-provider\backend && mvn spring-boot:run"

echo Entrusting Client Backend (Port 8085) 시작...
start "Entrusting Backend" cmd /c "cd entrusting-client\backend && mvn spring-boot:run"

echo.
echo [4/4] 프론트엔드 서비스 시작 중...
echo V-PASS Provider Frontend (Port 5176) 시작...
start "V-PASS Frontend" cmd /c "cd vpass-provider\frontend && npm run dev"

echo Entrusting Client Frontend (Port 5175) 시작...
start "Entrusting Frontend" cmd /c "cd entrusting-client\frontend && npm run dev"

echo.
echo ========================================================
echo 모든 서비스 실행 명령이 전달되었습니다.
echo 각 새 창에서 서비스가 정상적으로 뜨는지 확인해주세요.
echo.
echo 1. DB가 실행 중이어야 백엔드가 정상 동작합니다.
echo 2. 백엔드가 뜬 후 프론트엔드가 정상 동작합니다.
echo ========================================================
pause

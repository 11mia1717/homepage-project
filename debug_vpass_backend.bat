@echo off
setlocal
chcp 65001 >nul

set "MVN_CMD=C:\Program Files\Apache\maven\apache-maven-3.9.12-bin\bin\mvn.cmd"

echo [디버그] V-PASS Provider 백엔드를 단독 실행합니다.
echo [디버그] 경로: C:\ContinueProject\homepage-project\vpass-provider\backend
cd /d "C:\ContinueProject\homepage-project\vpass-provider\backend"

:: 환경변수 설정 (start_all_services.bat와 동일)
set "DB_URL=jdbc:mysql://localhost:3306/trustee_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
set "DB_USER=root"
set "DB_PASSWORD=password"

echo [디버그] 실행 명령어: mvn spring-boot:run
echo [디버그] 로그를 확인하세요.
echo.

if exist "%MVN_CMD%" (
    call "%MVN_CMD%" spring-boot:run
) else (
    echo [오류] Maven 명령어를 찾을 수 없습니다. PATH에 있는 mvn을 시도합니다.
    mvn spring-boot:run
)

pause

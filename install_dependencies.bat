@echo off
echo Setting up environment variables...
set PATH=%PATH%;C:\Program Files\nodejs;C:\Program Files\Apache\maven\apache-maven-3.9.12-bin\bin

echo Installing TM CallCenter Frontend Dependencies...
cd tm-center-tossbank/webapp/frontend
call npm install --legacy-peer-deps
if %errorlevel% neq 0 (
    echo npm install failed!
    pause
    exit /b %errorlevel%
)

echo.
echo Installing TM CallCenter Backend Dependencies...
cd ../backend
call mvn clean compile
if %errorlevel% neq 0 (
    echo mvn compile failed!
    pause
    exit /b %errorlevel%
)

echo.
echo ========================================================
echo All dependencies installed successfully!
echo You can now run start_services.bat
echo ========================================================
pause

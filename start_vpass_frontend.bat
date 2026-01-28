@echo off
REM Start V-PASS Frontend
echo Starting V-PASS Frontend on port 5176...
cd vpass-provider\frontend
set VITE_PORT=5176
set VITE_BACKEND_URL=http://localhost:8088
call npm install
call npm run dev

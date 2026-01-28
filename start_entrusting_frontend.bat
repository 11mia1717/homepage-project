@echo off
REM Start Entrusting Client Frontend
echo Starting Entrusting Client Frontend on port 5175...
cd entrusting-client\frontend
set VITE_PORT=5175
set VITE_BACKEND_URL=http://localhost:8085
set VITE_TRUSTEE_URL=http://localhost:8088
set VITE_TRUSTEE_FRONTEND_URL=http://localhost:5176
call npm install
call npm run dev

@echo off
echo Starting TM Center Frontend on port 5178...
cd tm-center-tossbank\webapp\frontend
set VITE_PORT=5178
set VITE_BACKEND_URL=http://localhost:8082
npm run dev

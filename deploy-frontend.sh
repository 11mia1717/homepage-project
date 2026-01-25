#!/bin/bash

# 설정 (수정 필요)
SERVER_IP="your_server_ip"
USERNAME="your_username"
REMOTE_PATH="/var/www/html/auth-company-frontend" # Nginx가 서빙할 경로

echo "프론트엔드 빌드 파일 배포 시작..."

# 빌드 폴더 전송
scp -r ./auth-company/frontend/dist/* $USERNAME@$SERVER_IP:$REMOTE_PATH

echo "프론트엔드 빌드 파일 전송 완료."
echo "Nginx 설정 및 서비스 재시작을 수동으로 진행해야 합니다."

#!/bin/bash

# --- 사용자 설정 변수 --- #
REPO_URL="https://github.com/11mia1717/homepage-project" # 프론트엔드 Git 리포지토리 URL (필수)
PROJECT_NAME="auth-company-frontend" # Git 클론 시 사용할 디렉토리 이름
FRONTEND_SUBDIR="auth-company/frontend" # Git 리포지토리 내에서 프론트엔드 코드의 서브 디렉토리 (필수)

# 배포 경로 설정
DEPLOY_BASE_PATH="/var/www/html" # Nginx가 서빙할 루트 경로
NGINX_ROOT_PATH="${DEPLOY_BASE_PATH}/${PROJECT_NAME}" # 실제 빌드 파일이 위치할 경로

# 백엔드 설정 (Nginx 프록시용)
BACKEND_IP="192.168.59.138" # 실제 백엔드 서버의 IP 주소 (필수)
BACKEND_PORT="8080" # 실제 백엔드 서버의 포트 (필수)
# --- 사용자 설정 변수 끝 --- #


# -------------------- 함수 -------------------- #
log_message() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] $1"
}

install_package() {
    PACKAGE_NAME=$1
    log_message "${PACKAGE_NAME} 설치 확인 및 설치 중..."
    if ! command -v ${PACKAGE_NAME} &> /dev/null
    then
        sudo apt update
        sudo apt install -y ${PACKAGE_NAME}
        if [ $? -ne 0 ]; then
            log_message "오류: ${PACKAGE_NAME} 설치에 실패했습니다."
            exit 1
        fi
        log_message "${PACKAGE_NAME} 설치 완료."
    else
        log_message "${PACKAGE_NAME}은(는) 이미 설치되어 있습니다."
    fi
}

# -------------------- 배포 스크립트 시작 -------------------- #
log_message "프론트엔드 Git 기반 배포 스크립트 시작..."

# 1. 필수 패키지 설치
install_package "git"
install_package "nodejs"
install_package "npm"
install_package "nginx"

# 2. Git 리포지토리 클론 또는 풀
PROJECT_DIR="$(pwd)/${PROJECT_NAME}"
log_message "프론트엔드 프로젝트 디렉토리: ${PROJECT_DIR}"

if [ -d "${PROJECT_DIR}" ]; then
    log_message "리포지토리 '${PROJECT_NAME}'이(가) 이미 존재합니다. 최신 변경 사항을 가져옵니다."
    cd "${PROJECT_DIR}/${FRONTEND_SUBDIR}"
    git pull
    if [ $? -ne 0 ]; then
        log_message "오류: Git pull 실패."
        exit 1
    fi
else
    log_message "리포지토리 '${PROJECT_NAME}'을(를) 클론합니다."
    git clone "${REPO_URL}" "${PROJECT_NAME}"
    if [ $? -ne 0 ]; then
        log_message "오류: Git 클론 실패. REPO_URL을 확인하세요."
        exit 1
    fi
    cd "${PROJECT_DIR}/${FRONTEND_SUBDIR}"
fi

# 3. 프론트엔드 애플리케이션 빌드
log_message "프론트엔드 종속성 설치 및 빌드 시작..."

npm install
if [ $? -ne 0 ]; then
    log_message "오류: npm install 실패."
    exit 1
fi
npm run build
if [ $? -ne 0 ]; then
    log_message "오류: npm run build 실패."
    exit 1
fi
log_message "프론트엔드 빌드 완료."

# 4. 빌드된 파일 Nginx 배포 경로로 이동
BUILD_DIR="${PROJECT_DIR}/${FRONTEND_SUBDIR}/dist" # 빌드 결과물이 dist에 있다고 가정
if [ ! -d "${BUILD_DIR}" ]; then
    log_message "오류: 빌드 디렉토리 '${BUILD_DIR}'를 찾을 수 없습니다. npm run build 결과물을 확인하세요."
    exit 1
fi

log_message "기존 Nginx 서빙 디렉토리 '${NGINX_ROOT_PATH}' 정리..."
sudo rm -rf "${NGINX_ROOT_PATH}"
sudo mkdir -p "${NGINX_ROOT_PATH}"

log_message "빌드된 파일을 '${NGINX_ROOT_PATH}'로 이동 중..."
sudo mv "${BUILD_DIR}"/* "${NGINX_ROOT_PATH}"/
if [ $? -ne 0 ]; then
    log_message "오류: 빌드된 파일 이동 실패."
    exit 1
fi
log_message "빌드된 파일 이동 완료."

# 5. Nginx 설정 파일 생성 또는 업데이트
NGINX_CONF_PATH="/etc/nginx/sites-available/${PROJECT_NAME}.conf"
NGINX_ENABLED_PATH="/etc/nginx/sites-enabled/${PROJECT_NAME}.conf"

log_message "Nginx 설정 파일 '${NGINX_CONF_PATH}' 생성/업데이트 중..."
cat <<EOF | sudo tee "${NGINX_CONF_PATH}"
server {
    listen 80;

    root ${NGINX_ROOT_PATH};
    index index.html index.htm;

    add_header X-Frame-Options "DENY";
    add_header X-Content-Type-Options "nosniff";
    add_header X-XSS-Protection "1; mode=block";
    add_header Referrer-Policy "no-referrer-when-downgrade";
    add_header Content-Security-Policy "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:; connect-src 'self' http://${BACKEND_IP}:${BACKEND_PORT};";

    location / {
        try_files \$uri \$uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://${BACKEND_IP}:${BACKEND_PORT};
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host \$host;
        proxy_cache_bypass \$http_upgrade;
        proxy_redirect off;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    error_page 500 502 503 504 /50x.html;
    location = /50x.html {
        root /usr/share/nginx/html;
    }
}
EOF

# 6. Nginx 설정 활성화 및 재시작
log_message "Nginx 설정 활성화 중..."
sudo ln -sf "${NGINX_CONF_PATH}" "${NGINX_ENABLED_PATH}"

log_message "Nginx 설정 구문 확인 중..."
sudo nginx -t
if [ $? -ne 0 ]; then
    log_message "오류: Nginx 설정에 문제가 있습니다. 확인하세요."
    exit 1
fi

log_message "Nginx 서비스 재시작 중..."
sudo systemctl restart nginx
if [ $? -ne 0 ]; then
    log_message "오류: Nginx 서비스 재시작에 실패했습니다."
    exit 1
fi

log_message "프론트엔드 배포 완료!"
# -------------------- 배포 스크립트 끝 -------------------- #

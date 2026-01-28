#!/bin/bash

# 로컬 서비스 통합 실행 스크립트 (안정화 버전)
echo "=================================="
echo "로컬 서비스 전체 실행 시작 (자율 복구 모드)"
echo "=================================="

# 프로젝트 루트 디렉토리로 이동
cd "$(dirname "$0")"

# logs 디렉토리가 없으면 생성
mkdir -p logs

# [.env] 설정 로드
if [ -f .env ]; then
    echo "📜 설정 파일(.env) 로드 중..."
    while IFS=\'=\' read -r key value || [ -n "$key" ]; do
        [[ $key =~ ^#.* ]] && continue
        [[ -z $key ]] && continue
        k=$(echo "$key" | tr -d \'\\r \' )
        v=$(echo "$value" | tr -d \'\\r\' | sed \'s/^ *//;s/ *$//\')
        if [ -n "$k" ]; then
            export "$k=$v"
        fi
    done < .env
fi

# JAVA_HOME 정규화 (끝의 \ 제거) - Maven 실행 오류 방지
if [ -n "$JAVA_HOME" ]; then
    export JAVA_HOME=$(echo "$JAVA_HOME" | sed \'s/[\\/]*$//\')
    echo "☕ JAVA_HOME: $JAVA_HOME"
fi

# 포트 변수 기본값 (from .env, 없으면 기본값)
T_BE=${TRUSTEE_BACKEND_PORT:-8088}
E_BE=${ENTRUSTING_BACKEND_PORT:-8085}
T_FE=${TRUSTEE_FRONTEND_PORT:-5176}
E_FE=${ENTRUSTING_FRONTEND_PORT:-5175}
TM_BE=${TM_BACKEND_PORT:-8082}
TM_FE=${TM_FRONTEND_PORT:-5178}

# 함수: 백엔드 서비스 시작
_start_backend() {
    local name=$1
    local dir=$2
    local port=$3
    echo "⚙️ $name Backend 시작 (:$port)..."
    cd "$dir"
    # PATH에 mvn이 있다고 가정하고 직접 호출
    cmd.exe /c "mvn clean spring-boot:run -Dspring-boot.run.arguments=--server.port=$port" > "../../logs/$(echo "$name" | tr \'[:upper:]\' \'[:lower:]\' | sed \'s/ /_//\')_backend.log" 2>&1 &
    cd ../..
}

# 함수: 프론트엔드 서비스 시작
_start_frontend() {
    local name=$1
    local dir=$2
    local port=$3
    local backend_url=$4
    local trustee_url=${5:-} # Optional
    local trustee_frontend_url=${6:-} # Optional

    echo "🖥️ $name Frontend 시작 (:$port)..."
    cd "$dir"
    local cmd_str="VITE_PORT=$port VITE_BACKEND_URL=$backend_url"
    if [ -n "$trustee_url" ]; then
        cmd_str="$cmd_str VITE_TRUSTEE_URL=$trustee_url"
    fi
    if [ -n "$trustee_frontend_url" ]; then
        cmd_str="$cmd_str VITE_TRUSTEE_FRONTEND_URL=$trustee_frontend_url"
    fi
    # PATH에 npm이 있다고 가정하고 직접 호출
    cmd.exe /c "$cmd_str npm run dev" > "../../logs/$(echo "$name" | tr \'[:upper:]\' \'[:lower:]\' | sed \'s/ /_//\')_frontend.log" 2>&1 &
    cd ../..
}

# 함수: MySQL 초기화
_run_mysql_init() {
    echo "🗄️ MySQL 데이터베이스 초기화 중... (init.sql 사용)"
    local db_host=${DB_HOST:-127.0.0.1}
    local db_port=${DB_PORT:-3307}
    local db_user=${DB_USER:-root}
    local db_password=${DB_PASSWORD:-password}
    local db_name=${DB_NAME:-entrusting_db}

    # Windows 명령 프롬프트에서 mysql 클라이언트 직접 실행
    cmd.exe /c "mysql -h $db_host -P $db_port -u $db_user -p$db_password $db_name < \"./homepage-project/init.sql\""
    if [ $? -eq 0 ]; then
        echo "✅ MySQL 데이터베이스 초기화 완료."
    else
        echo "❌ MySQL 데이터베이스 초기화 실패." >&2
    fi
}


# 기존 프로세스 정리
echo "🧹 기존 Java 프로세스 정리 중..."
# powershell.exe가 PATH에 있다고 가정하고 직접 호출
powershell.exe -Command "Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force"
echo "🧹 기존 Node.js 프로세스 정리 중..."
powershell.exe -Command "Get-Process node -ErrorAction SilentlyContinue | Stop-Process -Force"

echo ""
echo "[1/7] MySQL 확인 및 시작 중..."
# docker.exe가 PATH에 있다고 가정하고 직접 호출
if ! docker ps | grep -q shared-db; then
    docker compose up -d mysql
    echo "⏳ MySQL 서비스 시작 대기 중 (10초)..."
    sleep 10
fi
echo "✅ MySQL Ready (3307)"
_run_mysql_init # MySQL 초기화 함수 호출

echo ""
_start_backend "수탁사(V-PASS)" "vpass-provider/backend" "$T_BE"
echo ""
_start_backend "위탁사" "entrusting-client/backend" "$E_BE"
echo ""
_start_backend "TM 콜센터" "tm-center-tossbank/webapp/backend" "$TM_BE"

echo ""
echo "⏳ 백엔드 서비스 초기화 대기 중 (20초)..."
sleep 20

echo ""
_start_frontend "수탁사(V-PASS)" "vpass-provider/frontend" "$T_FE" "http://localhost:$T_BE"
echo ""
_start_frontend "위탁사" "entrusting-client/frontend" "$E_FE" "http://localhost:$E_BE" "http://localhost:$T_BE" "http://localhost:$T_FE"
echo ""
_start_frontend "TM 콜센터" "tm-center-tossbank/webapp/frontend" "$TM_FE" "http://localhost:$TM_BE"


echo ""
echo "⏳ 모든 서비스 초기화 대기 중 (30초)..."
sleep 30
echo "✅ 모든 서비스가 Background에서 실행 중입니다."
echo "📌 위탁사 페이지: http://localhost:$E_FE"
echo "📌 TM CallCenter 페이지: http://localhost:$TM_FE"

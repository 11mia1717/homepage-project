# @module Dockerfile (Frontend)
# @description React 프론트엔드 애플리케이션을 위한 Dockerfile입니다.
# Multi-stage build를 사용하여 개발 환경과 배포 환경을 분리하고, 최종 이미지 크기를 최적화합니다.

# k3s/컨테이너 전제 설명 주석
# 이 Dockerfile은 React 프론트엔드 애플리케이션을 컨테이너 이미지로 빌드합니다.
# 빌드된 이미지는 k3s(Kubernetes) 환경에서 Deployment로 배포되고,
# Service를 통해 내부적으로 노출되며, Ingress 또는 NodePort를 통해 외부 접근을 허용하도록 구성될 예정입니다.
# 개발 환경: Ubuntu (React)
# 목표: k3s 기반 컨테이너 환경에서 동작

# 1단계: 빌드 환경 설정
FROM node:20-alpine as builder

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm install

COPY . .

# VITE_BACKEND_API_URL 환경 변수를 빌드 시 주입
ARG VITE_BACKEND_API_URL
ENV VITE_BACKEND_API_URL=$VITE_BACKEND_API_URL

RUN npm run build

# 2단계: Nginx를 이용한 서빙 환경 설정
FROM nginx:alpine

# Nginx 설정 파일 복사
# [보안 취약점] Nginx 설정 파일 검토:
# Nginx 설정 파일(nginx.conf)에 불필요한 정보 노출, 취약한 HTTP 헤더 설정 등이 없는지 확인해야 합니다.
# 특히 SSL/TLS 설정(HTTPS 강제, 최신 TLS 버전 사용, 강력한 암호화 스위트)을 적절히 적용해야 합니다.
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 빌드된 React 애플리케이션 파일 복사
COPY --from=builder /app/dist /usr/share/nginx/html

# 컨테이너 실행 시 80번 포트 노출
EXPOSE 80

# Nginx 서버 시작
CMD ["nginx", "-g", "daemon off;"]

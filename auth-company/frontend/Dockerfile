# 빌더 스테이지: React 애플리케이션 빌드
FROM node:20-alpine AS builder

WORKDIR /app

# package.json 및 package-lock.json 복사 및 의존성 설치
COPY package.json .
COPY package-lock.json .
RUN npm install

# 모든 소스 파일 복사 및 React 애플리케이션 빌드
COPY . .
RUN npm run build

# 런타임 스테이지: Nginx를 사용하여 정적 파일 서빙
FROM nginx:alpine

# 빌더 스테이지에서 생성된 빌드 결과물을 Nginx 웹 루트로 복사
COPY --from=builder /app/dist /usr/share/nginx/html

# Nginx 설정 파일 복사
# 이 파일은 React 라우팅과 백엔드 API 프록시를 설정합니다.
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Nginx가 사용할 포트 노출
EXPOSE 80

# Nginx 서버 시작
CMD ["nginx", "-g", "daemon off;"]

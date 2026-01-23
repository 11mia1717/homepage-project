#!/bin/bash

# 프로젝트 디렉토리로 이동
cd auth-company/frontend

# 의존성 설치
npm install

# 프로젝트 빌드
npm run build

echo "Deployment setup complete! Build artifacts are in auth-company/frontend/dist/"

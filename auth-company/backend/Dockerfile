# @module Dockerfile (Backend)
# @description Spring Boot 백엔드 애플리케이션을 위한 Dockerfile입니다.
# Multi-stage build를 사용하여 개발 환경과 배포 환경을 분리하고, 최종 이미지 크기를 최적화합니다.

# k3s/컨테이너 전제 설명 주석
# 이 Dockerfile은 Spring Boot 백엔드 애플리케이션을 컨테이너 이미지로 빌드합니다.
# 빌드된 이미지는 k3s(Kubernetes) 환경에서 Deployment로 배포되고,
# Service를 통해 내부적으로 노출되며, Ingress 또는 NodePort를 통해 외부 접근을 허용하도록 구성될 예정입니다.
# 개발 환경: Windows (Spring Boot)
# 목표: k3s 기반 컨테이너 환경에서 동작

# 1단계: 빌더 스테이지 - Maven을 사용하여 애플리케이션 빌드
FROM maven:3.8.7-openjdk-17 AS builder

WORKDIR /app

# Maven 프로젝트 파일 복사 (의존성 다운로드를 위해 먼저 복사)
COPY pom.xml .
COPY src ./src

# Spring Boot 애플리케이션 빌드
# `mvn clean package -DskipTests` 명령은 테스트를 건너뛰고 패키징합니다.
# `target/*.jar`는 최종 빌드된 JAR 파일을 의미합니다.
RUN mvn clean package -DskipTests

# 2단계: 런타임 스테이지 - 가볍게 실행하기 위한 JRE 이미지 사용
# [보안 취약점] 컨테이너 이미지 보안 강화:
# 최소한의 런타임 환경을 제공하는 distroless 이미지(예: gcr.io/distroless/java:17)를 사용하면
# 공격 표면을 줄일 수 있습니다. 여기서는 교육용으로 openjdk를 사용합니다.
FROM openjdk:17-jre-slim

# 작업 디렉토리 설정
WORKDIR /app

# 빌더 스테이지에서 빌드된 JAR 파일 복사
# `backend-0.0.1-SNAPSHOT.jar`는 pom.xml에 정의된 artifactId와 version에 따라 달라질 수 있습니다.
# 실제 프로젝트에서는 `ls target/*.jar` 등을 통해 정확한 파일명을 확인하는 것이 좋습니다.
COPY --from=builder /app/target/backend-0.0.1-SNAPSHOT.jar ./app.jar

# 컨테이너 실행 시 8080번 포트 노출
EXPOSE 8080

# Spring Boot 애플리케이션 실행
# `java -jar app.jar` 명령으로 애플리케이션을 시작합니다.
# -Dspring.profiles.active=prod 와 같이 프로필을 지정할 수 있습니다.
CMD ["java", "-jar", "app.jar"]

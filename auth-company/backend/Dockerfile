# 빌더 스테이지
FROM maven:3.9.5-amazoncorretto-17 AS builder

WORKDIR /app

# Maven 의존성 캐싱을 위해 pom.xml 먼저 복사
COPY pom.xml .
RUN mvn dependency:go-offline

# 전체 소스 코드 복사 및 빌드
COPY src ./src
RUN mvn clean package -DskipTests

# 런타임 스테이지
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# 빌더 스테이지에서 빌드된 .jar 파일 복사
COPY --from=builder /app/target/*.jar app.jar

# Spring Boot 애플리케이션 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

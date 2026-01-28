@echo off
echo Starting Entrusting Client Backend on port 8085...
cd entrusting-client\backend
set MAVEN_HOME=C:\apache-maven-3.8.8
set PATH=%MAVEN_HOME%\bin;%PATH%
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8085

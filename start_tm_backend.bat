@echo off
echo Starting TM Center Backend on port 8082...
cd tm-center-tossbank\webapp\backend
set MAVEN_HOME=C:\apache-maven-3.8.8
set PATH=%MAVEN_HOME%\bin;%PATH%
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8082

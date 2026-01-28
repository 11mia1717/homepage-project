@echo off
echo Starting V-PASS Backend on port 8088...
cd vpass-provider\backend
set MAVEN_HOME=C:\apache-maven-3.8.8
set PATH=%MAVEN_HOME%\bin;%PATH%
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8088

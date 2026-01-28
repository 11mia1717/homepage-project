@echo off
echo Starting TM Center Backend on port 8082...
cd tm-center-tossbank\webapp\backend
"C:\Program Files\Apache\maven\apache-maven-3.9.12-bin\bin\mvn.cmd" spring-boot:run -Dspring-boot.run.arguments=--server.port=8082

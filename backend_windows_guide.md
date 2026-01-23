## Spring Boot 백엔드 (Windows Server) 배포 가이드

이 가이드는 `C:\ContinueProject\homepage-project\auth-company\backend` 경로에 있는 Spring Boot 애플리케이션을 `.jar` 파일로 빌드하고, Windows Server에 서비스로 등록하여 구동하는 방법을 설명합니다.

### 1. Spring Boot 애플리케이션 빌드 (.jar 파일 생성)

Windows Server로 이관하기 전에 로컬 개발 환경 (또는 CI/CD 환경)에서 Spring Boot 프로젝트를 빌드하여 실행 가능한 `.jar` 파일을 생성해야 합니다.

1.  **Maven 설치 확인:**
    명령 프롬프트 또는 PowerShell에서 다음 명령어를 실행하여 Maven이 설치되어 있는지 확인합니다.
    ```bash
    mvn -v
    ```
    설치되어 있지 않다면 [Maven 공식 사이트](https://maven.apache.org/download.cgi)에서 다운로드하여 설치하고 환경 변수를 설정해야 합니다.

2.  **프로젝트 디렉토리로 이동:**
    `C:\ContinueProject\homepage-project\auth-company\backend` 경로로 이동합니다.
    ```bash
    cd C:\ContinueProject\homepage-project\auth-company\backend
    ```

3.  **프로젝트 빌드:**
    다음 Maven 명령어를 실행하여 프로젝트를 빌드합니다.
    ```bash
    mvn clean package
    ```
    빌드가 성공적으로 완료되면, `target` 디렉토리 (`C:\ContinueProject\homepage-project\auth-company\backend\target`) 안에 `backend-0.0.1-SNAPSHOT.jar` (버전은 `pom.xml`에 따라 다를 수 있음)와 같은 이름의 실행 가능한 `.jar` 파일이 생성됩니다.

    **참고:** `auth-company/backend/target/backend-0.0.1-SNAPSHOT.jar` 파일을 Windows Server로 복사하여 사용합니다.

### 2. Windows Server에서 .jar 파일 실행

빌드된 `.jar` 파일을 Windows Server에 복사한 후, 명령 프롬프트에서 `java -jar` 명령을 사용하여 직접 실행할 수 있습니다.

1.  **Java 설치 확인:**
    Windows Server에 Java Runtime Environment (JRE) 또는 Java Development Kit (JDK)가 설치되어 있는지 확인합니다.
    ```bash
    java -version
    ```
    설치되어 있지 않다면 [Oracle](https://www.oracle.com/java/technologies/downloads/) 또는 [Adoptium](https://adoptium.net/)에서 다운로드하여 설치해야 합니다.

2.  **`.jar` 파일 복사:**
    로컬에서 빌드된 `backend-0.0.1-SNAPSHOT.jar` 파일을 Windows Server의 원하는 디렉토리 (예: `C:\homepage-backend\`)로 복사합니다.

3.  **명령 프롬프트에서 실행:**
    복사한 디렉토리로 이동하여 다음 명령어로 애플리케이션을 실행합니다.
    ```bash
    cd C:\homepage-backend\
    java -jar backend-0.0.1-SNAPSHOT.jar
    ```
    이 방법은 개발 및 테스트에 유용하지만, 서버 재부팅 시 자동으로 실행되지 않으며, 명령 프롬프트를 닫으면 애플리케이션도 종료됩니다.

### 3. Spring Boot 애플리케이션을 Windows 서비스로 등록 (NSSM 사용)

운영 환경에서는 Spring Boot 애플리케이션을 Windows 서비스로 등록하여 안정적으로 관리하는 것이 중요합니다. `NSSM` (Non-Sucking Service Manager)은 `.jar` 파일을 서비스로 쉽게 등록할 수 있게 해주는 도구입니다.

1.  **NSSM 다운로드:**
    [NSSM 공식 웹사이트](https://nssm.cc/download/)에서 NSSM을 다운로드하고 압축을 해제합니다. `win64` 또는 `win32` 폴더 내의 `nssm.exe` 파일을 Windows Server의 적절한 위치 (예: `C:\nssm\`)로 이동시킵니다.

2.  **NSSM을 사용하여 서비스 설치:**
    관리자 권한으로 명령 프롬프트 또는 PowerShell을 열고 `nssm.exe` 파일이 있는 디렉토리로 이동합니다.
    ```bash
    cd C:\nssm\
    nssm install [서비스이름]
    ```
    예시:
    ```bash
    nssm install HomepageBackendService
    ```

3.  **NSSM 설정:**
    `nssm install` 명령어를 실행하면 NSSM 서비스 설치 UI가 나타납니다. 다음 정보를 입력합니다:
    *   **Path:** `java.exe`의 경로를 지정합니다. (예: `C:\Program Files\Java\jdk-17\bin\java.exe`)
    *   **Startup directory:** `.jar` 파일이 있는 디렉토리입니다. (예: `C:\homepage-backend\`)
    *   **Arguments:** `-jar backend-0.0.1-SNAPSHOT.jar` (여기서 `backend-0.0.1-SNAPSHOT.jar`는 실제 `.jar` 파일 이름입니다.)

    *   **Details 탭:** Description, Display name 등을 설정할 수 있습니다.
    *   **Log on 탭:** 서비스 실행 계정을 설정할 수 있습니다. (기본값 Local System).
    *   **I/O 탭:** Output (stdout, stderr) 리다이렉션 경로를 설정하여 로그를 파일로 남길 수 있습니다. (예: `C:\homepage-backend\logs\stdout.log`, `C:\homepage-backend\logs\stderr.log`)

    설정 완료 후 `Install service` 버튼을 클릭합니다.

4.  **서비스 시작 및 관리:**
    서비스가 성공적으로 등록되면 Windows 서비스 관리자 (services.msc)에서 `HomepageBackendService` (또는 지정한 서비스 이름)를 확인할 수 있습니다. 서비스를 시작, 중지, 재시작할 수 있습니다.
    명령 프롬프트에서도 서비스 시작/중지가 가능합니다:
    ```bash
    net start HomepageBackendService
    net stop HomepageBackendService
    ```

이제 Spring Boot 백엔드 애플리케이션이 Windows Server에서 안정적인 서비스로 구동될 준비가 완료되었습니다.

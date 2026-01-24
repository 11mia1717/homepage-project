## React 프로젝트 Ubuntu 서버 이관 가이드

이 가이드는 현재 Windows 로컬 환경 (`c:/ContinueProject/homepage-project`)에 있는 React 프로젝트를 Ubuntu 서버의 `/var/www/html/homepage` 경로로 안전하고 효율적으로 이관하는 방법을 설명합니다. 프로젝트 구조는 `auth-company/frontend` (React)와 `auth-company/backend` (Spring Boot)로 구성되어 있습니다.

### 1. Git을 이용한 이관 (권장)

Git을 사용하면 버전 관리가 용이하고, 향후 업데이트 및 협업에 유리합니다.

#### 1.1. 로컬 (Windows) 환경 설정

1. **Git 저장소 초기화 및 커밋:**
   프로젝트 루트 디렉토리 (`c:\ContinueProject\homepage-project`)에서 Git 저장소를 초기화하고 모든 파일을 커밋합니다. (이미 Git 저장소라면 이 단계는 건너뜁니다.)
   ```bash
   cd c:\ContinueProject\homepage-project
   git init
   git add .
   git commit -m "Initial commit"
   ```
2. **원격 저장소 연결 및 푸시:**
   GitHub, GitLab 또는 사내 Git 서버 등 원격 Git 저장소를 생성하고 로컬 저장소를 연결한 후 코드를 푸시합니다.
   ```bash
   git remote add origin https://github.com/11mia1717/homepage-project.git
   git branch -M main
   git push -u origin main
   ```

#### 1.2. 원격 (Ubuntu) 서버 환경 설정

1. **Git 설치 (필요시):**
   Ubuntu 서버에 Git이 설치되어 있지 않다면 설치합니다.

   ```bash
   sudo apt update
   sudo apt install git -y
   ```
2. **프로젝트 클론:**
   `/var/www/html/` 경로로 이동하여 프로젝트를 클론합니다.

   ```bash
   cd /var/www/html/
   sudo git clone https://github.com/11mia1717/homepage-project.git homepage
   ```
3. **배포 스크립트 실행:**
   클론된 프로젝트 디렉토리 (`/var/www/html/homepage`)로 이동하여 `deploy-ready.sh` 스크립트에 실행 권한을 부여하고 실행합니다.

   ```bash
   cd /var/www/html/homepage
   chmod +x deploy-ready.sh
   ./deploy-ready.sh
   ```

   이 스크립트는 `auth-company/frontend` 디렉토리로 이동하여 `npm install` 및 `npm run build`를 수행하며, 빌드된 React 애플리케이션은 `auth-company/frontend/dist`에 생성됩니다.
4. **Nginx 설정 및 재시작:**
   제공된 `nginx-app.conf` 파일을 Nginx 설정 디렉토리로 복사하고, 심볼릭 링크를 생성하여 활성화한 다음 Nginx 서비스를 재시작합니다.

   ```bash
   sudo cp nginx-app.conf /etc/nginx/sites-available/homepage.conf
   sudo ln -s /etc/nginx/sites-available/homepage.conf /etc/nginx/sites-enabled/
   sudo nginx -t
   sudo systemctl restart nginx
   ```

### 2. SCP를 이용한 이관 (단순 복사)

Git 저장소를 사용하지 않고 단순히 파일을 복사하여 이관하는 방법입니다.

#### 2.1. 로컬 (Windows) 환경 설정

1. **프로젝트 압축:**
   `c:\ContinueProject\homepage-project` 디렉토리 전체를 압축합니다. (예: `homepage-project.zip`)
2. **파일 전송 (PowerShell 또는 Git Bash):**

   ```bash
   scp C:\ContinueProject\homepage-project.zip user@your_ubuntu_server_ip:/tmp/
   ```

   * `user`: Ubuntu 서버 사용자 이름
   * `your_ubuntu_server_ip`: Ubuntu 서버의 IP 주소

#### 2.2. 원격 (Ubuntu) 서버 환경 설정

1. **서버 접속 및 디렉토리 생성:**
   ```bash
   ssh user@your_ubuntu_server_ip
   cd /var/www/html/
   sudo mkdir homepage
   ```
2. **압축 파일 이동 및 해제:**
   ```bash
   sudo mv /tmp/homepage-project.zip homepage/
   cd homepage
   sudo apt install unzip -y # unzip이 설치되어 있지 않다면 설치
   sudo unzip homepage-project.zip
   # 압축 해제 후 생성된 최상위 폴더 내용 이동 및 삭제 (예시: homepage-project 폴더가 생성된 경우)
   sudo mv homepage-project/* .
   sudo rm -r homepage-project
   ```
3. **배포 스크립트 실행:**
   Git 이관과 동일하게 `deploy-ready.sh` 스크립트를 실행합니다.
   ```bash
   chmod +x deploy-ready.sh
   ./deploy-ready.sh
   ```
4. **Nginx 설정 및 재시작:**
   Git 이관과 동일하게 Nginx 설정을 복사하고 활성화한 다음 Nginx 서비스를 재시작합니다.
   ```bash
   sudo cp nginx-app.conf /etc/nginx/sites-available/homepage.conf
   sudo ln -s /etc/nginx/sites-available/homepage.conf /etc/nginx/sites-enabled/
   sudo nginx -t
   sudo systemctl restart nginx
   ```

### 3. 추가 고려사항

* **도메인 설정:** `nginx-app.conf` 파일의 `server_name your_domain.com www.your_domain.com;` 부분을 실제 운영할 도메인으로 변경해야 합니다.
* **HTTPS 설정:** 프로덕션 환경에서는 Let's Encrypt와 같은 서비스를 이용하여 HTTPS를 설정하는 것이 강력히 권장됩니다.
* **백엔드 서비스:** Nginx 설정은 `localhost:8080`으로 백엔드 API를 프록시하도록 되어 있습니다. 백엔드 서비스(예: Spring Boot 애플리케이션)가 Ubuntu 서버에서 이 포트로 실행되고 있는지 확인해야 합니다. 백엔드 애플리케이션도 유사한 방식으로 이관하고 실행해야 합니다.

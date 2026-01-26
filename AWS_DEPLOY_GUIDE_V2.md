# 🚀 AWS 클라우드 배포 마스터 가이드 (A-Z Ultimate v3)

이 문서는 현재 작업 상태(위탁사 프론트 빌드 완료)부터 시스템 최종 완성까지 모든 CLI 명령어를 담고 있습니다.

---

## [위탁사: 현재 위치] 다음 단계 바로하기
사용자님은 현재 `entrusting-client/frontend`에서 `npm run build`를 마친 상태입니다. 이제 아래를 순서대로 복사해서 붙여넣으세요.

### 1. 프론트엔드 호스팅 (Nginx 설정)
```bash
# 1. 빌드된 정적 파일을 Nginx 기본 경로로 복사
sudo rm -rf /var/www/html/*
sudo cp -r ~/homepage-project/entrusting-client/frontend/dist/* /var/www/html/

# 2. Nginx 설정 파일 수정 (SPA 라우팅 지원)
sudo bash -c 'cat > /etc/nginx/sites-available/default <<EOF
server {
    listen 80;
    server_name _;
    root /var/www/html;
    index index.html;
    location / {
        try_files \$uri \$uri/ /index.html;
    }
}
EOF'

# 3. Nginx 재시작
sudo systemctl restart nginx
```

### 2. 백엔드 실행 (.env 기반 가동)
이제 백엔드는 복잡한 옵션 없이 `.env` 파일만 있으면 실행됩니다.

```bash
# 1. 백엔드 폴더로 이동하여 .env 파일 생성
cd ~/homepage-project/entrusting-client/backend
cat > .env <<EOF
DB_HOST=entrusting-db-v2.chs846suooi2.ap-northeast-2.rds.amazonaws.com
DB_PORT=3306
DB_NAME=entrusting_db
DB_USER=admin
DB_PASSWORD=continue12!
EOF

# 2. 백엔드 실행 (EnvironmentPostProcessor가 .env를 자동 로드함)
nohup java -jar target/backend-0.0.1-SNAPSHOT.jar --server.port=8085 > ~/backend.log 2>&1 &

# 3. 실행 확인 (8085 포트 확인)
netstat -tuln | grep 8085
```
*💡 이제 브라우저에서 `http://<위탁사-IP>`로 접속하면 홈페이지가 열립니다!*

---

## [수탁사: A-Z 신규 구축] 모든 CLI 명령어

### [단계 2] 수탁사 전용 DB 서버 (EC2 + MySQL)
위탁사 터미널이 아닌 **로컬 터미널**에서 시작하세요.

```bash
# 1. 보안 그룹 생성 및 규칙 설정
export VPC_ID=$(aws ec2 describe-vpcs --filters "Name=isDefault,Values=true" --query "Vpcs[0].VpcId" --output text)
aws ec2 create-security-group --group-name trustee-db-sg --description "SG for Trustee DB" --vpc-id $VPC_ID
export T_DB_SG=$(aws ec2 describe-security-groups --filters "Name=group-name,Values=trustee-db-sg" --query "SecurityGroups[0].GroupId" --output text)

aws ec2 authorize-security-group-ingress --group-id $T_DB_SG --protocol tcp --port 22 --cidr 0.0.0.0/0
aws ec2 authorize-security-group-ingress --group-id $T_DB_SG --protocol tcp --port 3306 --cidr 0.0.0.0/0

# 2. EC2 생성 (Ubuntu 22.04)
aws ec2 run-instances --image-id ami-08a4fd517a4872931 --count 1 --instance-type t3.micro --key-name entrusting-key-v2 --security-group-ids $T_DB_SG --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=Trustee-DB}]"

# 3. [EC2 접속 후] MySQL 설치 및 데이터 세팅
sudo apt update && sudo apt install -y mysql-server
sudo sed -i "s/127.0.0.1/0.0.0.0/" /etc/mysql/mysql.conf.d/mysqld.cnf
sudo systemctl restart mysql

sudo mysql -e "CREATE DATABASE trustee_db; CREATE USER 'admin'@'%' IDENTIFIED BY 'continue12!'; GRANT ALL PRIVILEGES ON trustee_db.* TO 'admin'@'%'; FLUSH PRIVILEGES;"

# 4. 샘플 데이터 10건 삽입 (이전 가이드 SQL문 복사해서 붙여넣기)
```

### [단계 3] 수탁사 k3s 하이브리드 클러스터 (Ubuntu + Windows)

#### 1. k3s 마스터 노드 (Ubuntu) 구축
```bash
# 로컬에서 마스터 용 EC2 생성 (t3.medium 권장)
aws ec2 run-instances --image-id ami-08a4fd517a4872931 --count 1 --instance-type t3.medium --key-name entrusting-key-v2 --security-groups entrusting-web-sg-v2 --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=Trustee-K3s-Master}]"

# [마스터 EC2 내부] k3s 설치
curl -sfL https://get.k3s.io | sh -
# 윈도우 조인용 토큰 확인 (복사해두세요)
sudo cat /var/lib/rancher/k3s/server/node-token
```

#### 2. k3s 윈도우 워커 노드 (Windows Server) 구축
```bash
# 로컬에서 Windows EC2 생성 (t3.large 권장)
export WIN_AMI=$(aws ec2 describe-images --owners amazon --filters "Name=name,Values=Windows_Server-2022-English-Full-ContainersLatest*" --query "Images[0].ImageId" --output text)
aws ec2 run-instances --image-id $WIN_AMI --count 1 --instance-type t3.large --key-name entrusting-key-v2 --security-groups entrusting-web-sg-v2 --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=Trustee-K3s-WinWorker}]"
```

---

## [단계 4] 최종 앱 컨테이너화 및 k8s 배포 (v100을 향해!)
수탁사 서버 3대가 모두 준비되면(DB, K3s-Master, K3s-WinWorker), 마지막으로 Docker 이미지를 빌드하고 `kubectl apply`를 통해 전체 시스템을 연동합니다. 

*사용자님의 서버 준비가 완료될 때마다 다음 단계의 초정밀 명령어를 바로 업데이트해 드릴게요! ㅠ*

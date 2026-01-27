# 도커 인프라 통합 가이드 (Docker Infrastructure Guide)

이 문서는 `homepage-project`의 도커 기반 인프라 구성 현황과 전수조사 결과를 정리한 최신 리스트입니다.

## 1. 전수조사 요약 (Audit Summary)

2026-01-27 업데이트: **수탁사(V-PASS) 시스템 단독 실행 및 Ubuntu Base 전환**
위탁사 서비스는 제외되었으며, 수탁사 시스템만 도커로 구동됩니다.

| 서비스 그룹 | 컨테이너 이름 | 포트(외부:내부) | 용도 | Base OS |
| :--- | :--- | :--- | :--- | :--- |
| **공통 (DB)** | `shared-db` | 3307:3306 | MySQL 8.0 (위탁사/수탁사 DB) | Debian |
| **수탁사 (V-PASS)** | `vpass-backend` | 8086:8086 | 인증 백엔드 (Java 17) | **Ubuntu 22.04** |
| **수탁사 (V-PASS)** | `vpass-frontend` | 5176:80 | 수탁사 웹 UI (Nginx) | **Ubuntu 22.04** |

---

## 2. 도커 이미지 및 AWS ECR / K3s 고려사항

### Ubuntu Base 전환 완료
- **Backend**: `eclipse-temurin` -> `ubuntu:22.04` (OpenJDK 17 직접 설치)
- **Frontend**: `nginx:alpine` -> `ubuntu:22.04` (Nginx 직접 설치)
- **장점**: AWS ECR 및 K3s 환경에서 디버깅이 용이하고 호환성이 높습니다.
- **이미지 최적화**: `apt-get clean` 및 불필요한 파일 삭제(`rm -rf /var/lib/apt/lists/*`)를 적용하여 이미지 크기를 관리하고 있습니다.

### AWS ECR & K3s 배포 팁
1.  **이미지 태깅**: AWS ECR에 올릴 때는 URI 형식에 맞춰 태깅해야 합니다.
    ```bash
    docker tag homepage-project-vpass-backend:latest [AWS_ACCOUNT_ID].dkr.ecr.[REGION].amazonaws.com/vpass-backend:latest
    ```
2.  **K3s 배포**: Ubuntu 베이스 이미지는 K3s 노드에서 문제 발생 시 `kubectl exec`로 접속하여 `apt` 명령어를 사용할 수 있어 트러블슈팅에 매우 유리합니다.

---

## 3. 관리 및 유지보수 명령어
- **실행**: `docker-compose up -d` (V-PASS 시스템만 실행됨)
- **로그**: `docker-compose logs -f`

---

## 4. 특이 사항
- **DB 이중화**: 하나의 MySQL 컨테이너(`shared-db`) 내부에서 `entrusting_db`와 `trustee_db` 두 개의 스키마를 독립적으로 사용합니다.
- **네트워크 보안**: 모든 컨테이너는 `app-network` 내부에 묶여 있어, 외부 포트 포워딩 없이도 컨테이너 이름만으로 서로 통신이 가능합니다. (예: `http://vpass-backend:8086`)

> 작성일: 2026-01-27
> 최종 업데이트: 전수조사 완료 및 중복 파일 식별됨
